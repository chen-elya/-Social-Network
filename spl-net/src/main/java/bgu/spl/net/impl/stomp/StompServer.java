package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.StompMessagingProtocolImpl;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        ConnectionsImpl c= new ConnectionsImpl();
        if (args[1].equals("tpc"))
        {
            Server server = Server.threadPerClient(Integer.parseInt(args[0]),()-> new StompMessagingProtocolImpl(c),MessageEncoderDecoderImpl::new);
            server.serve();
        }
        else if(args[1].equals("reactor"))
        {
            Server.reactor((Integer.parseInt(args[0])),
                    Integer.parseInt(args[0]), //port
                    ()->new StompMessagingProtocolImpl(c),
                    MessageEncoderDecoderImpl::new).serve();
        }
    }


}
