package bgu.spl.net.api;


import bgu.spl.net.srv.ConnectionsImpl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class StompMessagingProtocolImpl<T> implements StompMessagingProtocol<T> {
    private int connectionId;
    private ConnectionsImpl<String> connections;
    private boolean terminate;
    private static AtomicInteger receipt = new AtomicInteger(0);

    public StompMessagingProtocolImpl(ConnectionsImpl c)
    {
        connections= c;
    }
    public void start(int connectionId, ConnectionsImpl<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.terminate = false;
    }


    public void process(String message) {
        receipt.compareAndSet(receipt.get(),receipt.get()+1);
        if (message != null) {
            System.out.println(message);
            String lines[] = message.split("\\r?\\n");
            //------------------Connect-----------------------------------------------------------------------------------------------
            if ("CONNECT".equals(lines[0])) {
                if (lines[1].substring(0, lines[1].indexOf(":")).equals("accept-version")) {
                    if (lines[2].substring(0, lines[2].indexOf(":")).equals("host")) {
                        if (lines[3].substring(0, lines[3].indexOf(":")).equals("login")) {
                            if ((lines[4].substring(0, lines[4].indexOf(":")).equals("passcode"))) {
                                //Connect Client Checks the following:
                                //1.Checks if the client is already with another user
                                //2.Checks if the username + passcode is correct
                                //3.Checks if the user is already connected
                                //Returns "CONNECTED" if there is no error, else returns an appropriate error
                                String s = connections.ConnectClient(lines[3].substring(lines[3].indexOf(":")+1), lines[4].substring(lines[4].indexOf(":")+1), connectionId);
                                if (s.equals("CONNECTED")) {
                                    connections.send(connectionId, s + "\n" + lines[1].substring(lines[1].indexOf("-")+1));
                                } else connections.send(connectionId, Error(message, s));
                            } else
                                connections.send(connectionId, Error(message, "Did not contain passcode in the CONNECT"));
                        } else
                            connections.send(connectionId, Error(message, "Did not contain login in the CONNECT"));
                    } else connections.send(connectionId, Error(message, "Did not contain host in the CONNECT"));
                } else
                    connections.send(connectionId, Error(message, "Did not contain accept-version in the CONNECT"));

//--------------------SEND-----------------------------------------------------------------------------------------------
            } else if ("SEND".equals(lines[0])) {
                String words[] = lines[3].split(" ");
                if (lines[1].substring(0, lines[1].indexOf(":")).equals("destination")) {
                    boolean added =words.length>2 && words[2].equals("added");
                    boolean rest = (!connections.getsubscribeMap().isEmpty()&&connections.getsubscribeMap().get(connectionId).containsKey(lines[1].substring(lines[1].indexOf(":")+1)));
                    if (((!added) & rest)|| (added && rest))  {
                        MessageFrame mf = new MessageFrame(); //creating a new message
                        mf.setDestination(lines[1].substring(lines[1].indexOf(":")+1));
                        String body = "";
                        for (int i = 2; i < lines.length; i++) {
                            body = body + lines[i] + "\n";
                        }
                        System.out.println(body);
                        mf.setBody(body);
                        //we get the Subscribemap which is a hashmap <connectionId,<channel,channel number>>
                        //and we take the channel id from it.
                        mf.setSubscription(connections.getsubscribeMap().get(connectionId).get(mf.getDestination()));
                        connections.send((lines[1].substring(lines[1].indexOf(":")+1)), mf.toString());
                    } else if(!added) {
                        connections.send(connectionId, Error(message, "the client tries to send a message to a destination which it is not subscribed to"));
                    }
                } else connections.send(connectionId, Error(message, "Did not contain destination in the SEND"));
                //-------------------SUBSCRIBE-----------------------------------------------------------------------------------------------
            } else if ("SUBSCRIBE".equals(lines[0])) {
                if (lines[1].substring(0, lines[1].indexOf(":")).equals("destination")) {
                    if (lines[2].substring(0, lines[2].indexOf(":")).equals("id")) {
                        if (lines[3].substring(0, lines[3].indexOf(":")).equals("receipt")) {
                            //adds the user to the requested destination via id
                            connections.subscribeClient(lines[1].substring(lines[1].indexOf(":")+1), connectionId);
                            Receipt receipt = new Receipt(Integer.parseInt(lines[3].substring(lines[3].indexOf(":")+1)));
                            connections.send(connectionId, receipt.toString());
                        } else
                            connections.send(connectionId, Error(message, "Did not contain receipt in the SUBSCRIBE"));
                    } else connections.send(connectionId, Error(message, "Did not contain id in the SUBSCRIBE"));
                } else
                    connections.send(connectionId, Error(message, "Did not contain destination in the SUBSCRIBE"));
                //-------------------UNSUBSCRIBE-----------------------------------------------------------------------------------------------
            } else if ("UNSUBSCRIBE".equals(lines[0])) {
                boolean b =false;
                if (lines[1].substring(0, lines[1].indexOf(":")).equals("destination")) {
                    if (lines[2].substring(0, lines[2].indexOf(":")).equals("id")) {
                        if (lines[3].substring(0, lines[3].indexOf(":")).equals("receipt")) {
                            boolean rest = (!connections.getsubscribeMap().isEmpty()&&connections.getsubscribeMap().get(connectionId).containsKey(lines[1].substring(lines[1].indexOf(":")+1)));
                            if (rest) {
                                b = connections.unsubscribeClient(connections.getsubscribeMap().get(connectionId).get(lines[1].substring(lines[1].indexOf(":") + 1)), connectionId);
                            }
                            if (b) {
                                Receipt receipt = new Receipt(Integer.parseInt(lines[3].substring(lines[3].indexOf(":") + 1)));
                                connections.send(connectionId, receipt.toString());
                            }
                            else connections.send(connectionId, Error(message, "Couldn't UNSUBSCRIBE the Client"));
                        } else
                            connections.send(connectionId, Error(message, "Did not contain receipt in the UNSUBSCRIBE"));
                    } else connections.send(connectionId, Error(message, "Did not contain id in the UNSUBSCRIBE"));
                } else
                    connections.send(connectionId, Error(message, "Did not contain destination in the UNSUBSCRIBE"));

                //-------------------DISCONNECT-----------------------------------------------------------------------------------------------
            } else if ("DISCONNECT".equals(lines[0])) {
                if (lines[1].substring(0, lines[1].indexOf(":")).equals("receipt")) {
                    Receipt receipt = new Receipt(Integer.parseInt(lines[1].substring(lines[1].indexOf(":")+1)));
                    connections.send(connectionId, receipt.toString());
                    connections.disconnect(connectionId);
                } else connections.send(connectionId, Error(message, "Did not contain receipt in the DISCONNECT"));
            }
            else
            {
                connections.send(connectionId, Error(message, "Invalid message"));
                }
            }
        }

    private String Error(String msg, String error) {
        return "ERROR\n" +
                "receipt-id:message-" + receipt + "\n" +
                "message:" + error + "\n"+
                "-----------"+"\n"
                + msg;
    }

    public boolean shouldTerminate() {
        return terminate;
    }
}
