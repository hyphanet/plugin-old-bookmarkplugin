package falimat.freenet.bookmarkplugin.model;

public class Slot {

    private String channel;

    private int index;

    private boolean available = false;

    private boolean invalid = false;
    
    private int failureCount = 0;

    private long expectedTime;

    private long requestTime;

    private String sender;

    public Slot() {
        
    }
    
    public Slot(Channel channel, int index) {
       this.channel = channel.getId();
       this.sender = channel.getSender();
       this.index = index;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean inserted) {
        this.available = inserted;
    }

    public int getFailureCount() {
        return this.failureCount;
    }

    public void setFailureCount(int insertFailures) {
        this.failureCount = insertFailures;
    }

    public long getRequestTime() {
        return this.requestTime;
    }

    public void setRequestTime(long insertTime) {
        this.requestTime = insertTime;
    }

    public String getUri() {
        return this.channel + "-" + this.index;
    }

    public long getExpectedTime() {
        return this.expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        this.expectedTime = expectedTime;
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

}
