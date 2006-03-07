package falimat.freenet.bookmarkplugin.model;

public class Pong extends Ping {
    private long pingInsertTime;

    private long pingFetchTime;

    private String pingId;

    public Pong() {

    }

    public Pong(Ping ping, User user) {
        this.sender = user.getPublicSSK();
        this.pingId = ping.getId();
        this.pingInsertTime = ping.getInsertTime();
        this.pingFetchTime = System.currentTimeMillis();
        this.insertTime = System.currentTimeMillis();
    }

    public long getPingFetchTime() {
        return this.pingFetchTime;
    }

    public void setPingFetchTime(long pingFetchTime) {
        this.pingFetchTime = pingFetchTime;
    }

    public long getPingInsertTime() {
        return this.pingInsertTime;
    }

    public String getPingId() {
        return this.pingId;
    }

    public void setPingId(String pingId) {
        this.pingId = pingId;
    }

    public void setPingInsertTime(long pingInsertTime) {
        this.pingInsertTime = pingInsertTime;
    }

}
