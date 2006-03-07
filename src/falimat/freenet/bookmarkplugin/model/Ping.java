package falimat.freenet.bookmarkplugin.model;

import java.util.LinkedList;
import java.util.List;

public class Ping extends AbstractSendable {

    private List<String> recipientList = new LinkedList<String>();

    protected long insertTime;

    public long getInsertTime() {
        return this.insertTime;
    }

    public void setInsertTime(long insertTime) {
        this.insertTime = insertTime;
    }

    public List<String> getRecipientList() {
        return this.recipientList;
    }

    public void setRecipientList(List<String> recipientList) {
        this.recipientList = recipientList;
    }

    public String getId() {
        return super.getSender() + "-" + this.getInsertTime();
    }
}
