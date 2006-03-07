package falimat.freenet.bookmarkplugin.model;

public class AbstractSendable {
    String sender;

    private boolean published = false;

    private long lastModified;

    public String getSender() {
        return this.sender;
    }

    public void setSender(String user) {
        this.sender = user;
    }

    public boolean isPublished() {
        return this.published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(long time) {
        this.lastModified = time;
    }

}
