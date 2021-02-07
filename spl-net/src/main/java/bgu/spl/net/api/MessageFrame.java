package bgu.spl.net.api;
public class MessageFrame implements Message {
    private int subscription;
    private static int messageId=0;
    private String destination;
    private String body;

    public MessageFrame() {
        this.subscription = -1;
        this.messageId++;
        this.destination = null;
        this.body = null;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String content) {
        this.body = content;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSubscription() {
        return subscription;
    }

    public void setSubscription(int subscription) {
        this.subscription = subscription;
    }

    public String toString()
    {

        return "MESSAGE\n"+
                "subscription:"+ getSubscription()+"\n"+
                "Message=id:"+ getMessageId()+"\n"+
                "destination:"+getDestination()+"\n"+
                getBody();
    }
}


