package bgu.spl.net.srv;

import bgu.spl.net.api.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionsImpl<T> implements Connections<T> {
    //HashMap that saves pairs of <connectionId,connectionHandler> of the clients
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> idPerClients;
    //Hashmap that has all the connectionId's of all the users that are subscribed to the channels
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> channelMap;

    // We a hashmap in a hashmap because when we get UNSUBSCRIBE we only get the Id of the channel and this id is not the
    // same for every user.
    //                    connectionId                    Channel,subscriptionId
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> subscribeMap;
    private CopyOnWriteArrayList<User> userList; //Thread Safe List
    private static int subscribenumber = 1;

    public ConnectionsImpl() {
        idPerClients = new ConcurrentHashMap();
        channelMap = new ConcurrentHashMap();
        channelMap = new ConcurrentHashMap();
        subscribeMap = new ConcurrentHashMap();
        userList = new CopyOnWriteArrayList();
    }

    public boolean send(int connectionId, T msg) {
        //We synchronized this because we want to make one send at a time
        //And we don't want to synchronize idPerClient because we just and we dont change anything in there
        synchronized (this) {
            if (idPerClients.get(connectionId) != null) {
                //send of the Connection Handler
                idPerClients.get(connectionId).send(msg);
                return true;
            }
            return false;
        }
    }

    public void send(String channel, T msg) {
        CopyOnWriteArrayList<Integer> channelQ = channelMap.get(channel);
        if (channelQ != null) {
            for (Integer id : channelQ) {
                //send of the ConnectionsImpl
                send(id, msg);
            }
        }
    }

    public void disconnect(int connectionId) {
        if (channelMap != null) {
            //We synchronized the ChannelMap to avoid the case that we try to change something that is not in the map
            //anymore. (change something that is null)
            synchronized (channelMap) {
                if (channelMap.get(connectionId) != null) {
                    channelMap.remove(connectionId);
                }
            }
        }
                for (User user : userList) {
                    if (user.getConnectionId() == connectionId) {
                        user.setStatus(false);
                    }
                }
                    if (idPerClients != null && idPerClients.get(connectionId) != null)
                    {
                        //We synchronized the idPerClient to avoid the case that we try to change something that is not in the map
                        //anymore. (change something that is null)
                        synchronized (idPerClients) {
                            idPerClients.remove(connectionId);
                        }
                    }
                }
    //Checks the following:
    //1.Checks if the client is already with another user
    //2.Checks if the username + passcode is correct
    //3.Checks if the user is already connected
    //Returns "CONNECTED" if there is no error, else returns an appropriate error
    public String ConnectClient(String name, String passcode, int connectionId) {
        String output = "";
        boolean found = false;
        //1.checks if there is a user with the same id of the connectionId, if so the Client have connected already with another user
        for (User user : userList) {
            if (user.getConnectionId() == connectionId && user.getStatus())
                found = true;
        }
        if (!found) { //The Client didn't connect with another user
            for (User user : userList) {
                if (user.getConnectionId() != connectionId) {
                    if (user.getName().equals(name)) { //checks if the is a name already registered to the server
                        found = true;
                        if (user.getPasscode().equals(passcode)) { //2.checks if the passcode is correct
                            if (user.getStatus() == false) { //3.checks if the user is connected
                                output = "CONNECTED";
                                user.setStatus(true);
                                user.setConnectionId(connectionId);
                            } else output = "User already logged in";
                        } else output = "Wrong password";
                    }
                }
            }
            if (!found) { //if we didnt find the user's name, meaning we register a new user
                User user = new User(name, passcode, connectionId);
                userList.add(user);
                output = "CONNECTED";
            }
        } else output = "Tried to log in while the Client already logged in with another user";
        return output;
    }

    public void addClient(int id, ConnectionHandler<T> ch) {
        idPerClients.putIfAbsent(id, ch);
    }

    public void subscribeClient(String channel, int connectionId) {
        if (subscribeMap == null || subscribeMap.get(connectionId) == null) {
            subscribeMap.putIfAbsent(connectionId, new ConcurrentHashMap());
        }
        subscribeMap.get(connectionId).putIfAbsent(channel, subscribenumber);
        subscribenumber++;
        if(!channelMap.contains(channel))
        {
            channelMap.putIfAbsent(channel,new CopyOnWriteArrayList<>());
        }
        channelMap.get(channel).add(connectionId);
    }

    public boolean unsubscribeClient(int subscriptionId,int connectionId) {
        String channel = "";
        //iterating on all the channels to find the channel to remove the user from it's channelMap
        for (String key : subscribeMap.get(connectionId).keySet()) {
            if (subscribeMap.get(connectionId).get(key) == subscriptionId) {
                channel = key;
            }
        }
        //subscribeMap.get(ConnectionId) = ConcurrentHashMap <String,Integer>
        //We synchronized the subscribeMap.get(ConnectionId) to avoid the case that we try to change something that is not in the map
        //anymore. (change something that is null)
        synchronized (subscribeMap.get(connectionId)) {
            subscribeMap.get(connectionId).remove(channel); //removing the user from the subscribeMap
        }
        if (channel != "") {
            //We synchronized the channelMap to avoid the case that we try to change something that is not in the map
            //anymore. (change something that is null)
            synchronized (channelMap.get(channel)) {
                Iterator it = channelMap.get(channel).iterator();
                int i=0;
                while (it.hasNext()) {
                    if (it.next().equals(connectionId)) {
                        channelMap.get(channel).remove(i);
                    }
                    i++;
                }
                return true;
            }
        }
        return false;
    }
        public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> getsubscribeMap ()
        {
            return subscribeMap;
        }

}
