package bgu.spl.net.api;

import java.util.concurrent.atomic.AtomicBoolean;

public class User {
    private String name;
    private String passcode;
    private AtomicBoolean status;
    private int connectionId;
    public User(String name,String passcode,int connectionId)
    {
        this.name = name;
        this.passcode = passcode;
        this.status = new AtomicBoolean(true);
        this.connectionId = connectionId;
    }
    public boolean getStatus()
    {
        return this.status.get();
    }
    public void setStatus (boolean b)
    {
        this.status.set(b);
    }

    public String getPasscode() {
        return passcode;
    }

    public String getName() {
        return name;
    }

    public int getConnectionId() {
        return connectionId;
    }
    public void setConnectionId(int connectionId)
    {
        this.connectionId = connectionId;
    }
}
