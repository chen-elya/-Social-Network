package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

//    void addClient(int id,ConnectionHandler<T> ch);
//
//     void subscribeClient(String channel, int connectionId);
//
//     boolean unsubscribeClient(int subscriptionId,int connectionId);
//
//    ConcurrentHashMap<Integer, ConcurrentHashMap <String,Integer>> getsubscribeMap();
//
//    String  ConnectClient(String name,String passcode,int connectionId);

}
