package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessageFrame;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.Receipt;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

public class EchoProtocol implements MessagingProtocol<String> {
    private int connectionId;
    private ConnectionsImpl<String> connections;
    private boolean terminate;
    private static int receipt = 0;

    public void start(int connectionId, ConnectionsImpl<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.terminate = false;
    }

    @Override
    public String process(String message) {
        String response = null;
        receipt++;
        if (message != null) {
            String lines[] = message.split("\\r?\\n");
            switch (lines[0]) {
//------------------Connect-----------------------------------------------------------------------------------------------
                case "CONNECT":
                    if (lines[1].substring(0, lines[1].indexOf(":")).equals("accept-version")) {
                        if (lines[2].substring(0, lines[2].indexOf(":")).equals("host")) {
                            if (lines[3].substring(0, lines[3].indexOf(":")).equals("login")) {
                                if ((lines[4].substring(0, lines[4].indexOf(":")).equals("passcode"))) {
//////////////////////////////////DONT KNOW YET HOW TO Do IT///////////////////////////
                                } else
                                    response= Error(message, "Did not contain passcode in the CONNECT");
                            } else
                                response = Error(message, "Did not contain login in the CONNECT");
                        } else response = Error(message, "Did not contain host in the CONNECT");
                    } else
                        response = Error(message, "Did not contain accept-version in the CONNECT");
                    break;

//--------------------SEND-----------------------------------------------------------------------------------------------
                case "SEND":
                    if (lines[1].substring(0, lines[1].indexOf(":")).equals("destination")) {
                        if (connections.getsubscribeMap().get(connectionId).contains(lines[1].substring(lines[1].indexOf(":"))))
                        {
                            MessageFrame mf = new MessageFrame(); //creating a new message
                            mf.setDestination(lines[1].substring(lines[1].indexOf(":")));
                            String body = "";
                            for (int i = 2; i < lines.length; i++) {
                                body = body + lines[i] + "\n";
                            }
                            mf.setBody(body);
                            //we get the Subscribemap which is a hashmap <connectionId,<channel,channel number>>
                            //and we take the channel id from it.
                            mf.setSubscription(connections.getsubscribeMap().get(connectionId).get(mf.getDestination()));
                            connections.send((lines[1].substring(lines[1].indexOf(":"))), mf.toString());
                        } else
                            response = Error(message, "the client tries to send a message to a destination which it is not subscribed to");
                    } else response = Error(message, "Did not contain destination in the SEND");
                    break;
                //-------------------SUBSCRIBE-----------------------------------------------------------------------------------------------
                case "SUBSCRIBE":
                    if (lines[1].substring(0, lines[1].indexOf(":")).equals("destination")) {
                        if (lines[2].substring(0, lines[2].indexOf(":")).equals("id")) {
                            if (lines[3].substring(0, lines[3].indexOf(":")).equals("receipt")) {
                                //adds the user to the requested destination via id
                                connections.subscribeClient(lines[1].substring(lines[1].indexOf(":")), connectionId);
                                Receipt receipt = new Receipt(Integer.parseInt(lines[3].substring(lines[3].indexOf(":"))));
                                response =receipt.toString();
                            } else
                                response = Error(message, "Did not contain receipt in the SUBSCRIBE");
                        } else response = Error(message, "Did not contain id in the SUBSCRIBE");
                    } else
                        response = Error(message, "Did not contain destination in the SUBSCRIBE");
                    break;
                //-------------------UNSUBSCRIBE-----------------------------------------------------------------------------------------------
                case "UNSUBSCRIBE":
                    if (lines[1].substring(0, lines[1].indexOf(":")).equals("id")) {
                        boolean b = connections.unsubscribeClient(Integer.parseInt(lines[1].substring(lines[1].indexOf(":"))), connectionId);
                        if (!b)
                            response = Error(message, "Subscription doesn't exist for this client");
                    } else
                        response = Error(message, "Did not contain id in the UNSUBSCRIBE");
                    break;
                //-------------------DISCONNECT-----------------------------------------------------------------------------------------------
                case "DISCONNECT":
                    if (lines[1].substring(0, lines[1].indexOf(":")).equals("receipt")) {
                        Receipt receipt = new Receipt(Integer.parseInt(lines[1].substring(lines[1].indexOf(":"))));
                        connections.disconnect(connectionId);
                        response = receipt.toString();
                        terminate=true;
                    } else response = Error(message, "Did not contain receipt in the DISCONNECT");
                    break;
            }
        }
        return "";
    }

    private String Error(String msg, String error) {
        return "ERROR\n" +
                "receipt-id:message-" + receipt + "\n" +
                "message:" + error + "\n"
                + msg;
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}

//    private boolean shouldTerminate = false;
//
//    @Override
//    public void process(String msg) {
//
//    }
//}
////        shouldTerminate = "bye".equals(msg);
////        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
////        return createEcho(msg);
////    }
////
////    private String createEcho(String message) {
////        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
////        return message + " .. " + echoPart + " .. " + echoPart + " ..";
////    }
////
////    @Override
////    public boolean shouldTerminate() {
////        return shouldTerminate;
////    }
////}
