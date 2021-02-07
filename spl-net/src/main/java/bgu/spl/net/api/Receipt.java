package bgu.spl.net.api;
public class Receipt implements Message {
    private int id;
    private boolean logout;
    public Receipt(int id)
    {
        this.id=id;
        this.logout = false;
    }
    public void setLogout(boolean logout)
    {
        this.logout = logout;
    }
    public int getId() {
        return id;
    }
    public String toString()
    {
        if(!logout)
        return "RECEIPT\n" + "receipt-id:"+getId();
        else
            return "RECEIPT\n" + "receipt-id: "+getId();
    }

}


