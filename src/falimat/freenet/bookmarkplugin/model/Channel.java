package falimat.freenet.bookmarkplugin.model;

import java.io.Serializable;

public class Channel extends AbstractSendable implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2560470916611131394L;

    public static Channel A = new Channel();

    public static Channel B = new Channel();

    static {
        A.sender = "SSK@5xiiXlXU16c3ERR4mBEH-KKSnWHH8TWJlY0og8Lb2YI,m1WIfXV-pDMLy1GYYSmBgj98u~y9PPYwmq-vLqVUFf4,AQABAAE/";
        A.basename = "tags";
        A.lastSlot = 0;
        A.lastInsertTime = 0;
        A.insertInterval = 5 * 60 * 1000;

        B.sender = "SSK@6IBcICgd8hXMnIBUOO9LXwrm0YTG0s6YkGRt0-Hgwm4,sNACThZojXbr8DrMuXD5VEsOvkD5jx3-ckbue0-q9AM,AQABAAE/";
        B.basename = "tags";
        B.lastSlot = 3;
        B.lastInsertTime = System.currentTimeMillis();
        B.insertInterval = 1 * 60 * 1000;
    }

    private String basename;

    private int lastSlot = -1;

    private long lastInsertTime = -1;

    private int insertInterval = 10 * 60 * 1000;

    public String getBasename() {
        return this.basename;
    }

    public void setBasename(String baseName) {
        this.basename = baseName;
    }

    public int getInsertInterval() {
        return this.insertInterval;
    }

    public void setInsertInterval(int insertInterval) {
        this.insertInterval = insertInterval;
    }

    public long getLastInsertTime() {
        return this.lastInsertTime;
    }

    public void setLastInsertTime(long lastInsertTime) {
        this.lastInsertTime = lastInsertTime;
    }

    public int getLastSlot() {
        return this.lastSlot;
    }

    public void setLastSlot(int lastSlot) {
        this.lastSlot = lastSlot;
    }

    public String getId() {
        return this.sender + "/" +this.basename;
    }

    public Slot createNextSlot() {
        this.lastSlot++;
        Slot slot = new Slot(this, this.lastSlot);
        slot.setExpectedTime(System.currentTimeMillis());
        this.lastInsertTime = slot.getExpectedTime();
        this.setPublished(false);
        return slot;
    }
}
