package falimat.freenet.network;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import xomat.util.xml.XmlUtils;
import falimat.freenet.bookmarkplugin.model.AbstractSendable;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.Ping;
import falimat.freenet.bookmarkplugin.model.Pong;
import falimat.freenet.bookmarkplugin.model.Slot;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.crypt.CryptoUtil;

public class XmlSlotWriter implements SlotWriter {

    static final String ELEMENT_PING_ID = "pingId";

    static final String ELEMENT_PING_FETCH_TIME = "pingFetchTime";

    static final String ELEMENT_PING_INSERT_TIME = "pingInsertTime";

    static final String ELEMENT_INSERT_TIME = "insertTime";

    private static final String ELEMENT_CONTENT_TYPE = "contentType";

    static final String ELEMENT_SIZE = "size";

    static final String ELEMENT_LAST_MODIFIED = "lastModified";

    static final String ELEMENT_RATING = "rating";

    static final String ELEMENT_DESCRIPTION = "description";

    static final String ELEMENT_TITLE = "title";

    static final String ELEMENT_URI = "uri";

    public static final String ATTR_SIGNATURE = "signature";

    public static final String ELEMENT_KEYS = "keys";

    public static final String PUBLIC_DH = "public-dh";

    public static final String PUBLIC_DSA = "public-dsa";

    public static final String PUBLIC_SSK = "public-ssk";

    private final static Log log = LogFactory.getLog(XmlSlotWriter.class);

    public final static String CONTENT_TYPE = "application/xml;falimat-version=0.1";

    final static String TYPE_BOOKMARK = "bookmark";

    public static final String ELEMENT_USER = "user";

    public static final String ATTR_NAME = "name";

    static final String TYPE_PING = "ping";

    static final String TYPE_PONG = "pong";

    public static final String ELEMENT_BASENAME = "basename";

    public static final String ELEMENT_LAST_SLOT = "lastSlot";

    public static final String ELEMENT_LAST_INSERT_TIME = "lastInsertTime";

    public static final String ELEMENT_INSERT_INTERVAL = "insertInterval";

    public static final String TYPE_CHANNEL = "channel";

    private User inserter;

    private Element slotElement;

    private Document slotDocument;

    private Slot slot;

    public XmlSlotWriter() {
        this.slotDocument = DocumentFactory.getInstance().createDocument();
        this.slotElement = DocumentFactory.getInstance().createElement("slot");
        this.slotDocument.setRootElement(this.slotElement);
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
        this.slotElement.addAttribute(ELEMENT_URI, slot.getUri());
        this.slotElement.addAttribute("insert-time", Long.toString(slot.getRequestTime()));
    }

    public String getInsertUri() {
        String publicUri = slot.getUri();
        return inserter.getPrivateSSK() + publicUri.substring(publicUri.indexOf('/'));
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }

    public void setKeypair(User inserter) {

        this.inserter = inserter;

        Element userElement = this.slotElement.addElement(ELEMENT_USER);
        userElement.addAttribute(PUBLIC_SSK, inserter.getPublicSSK());
        userElement.addAttribute(PUBLIC_DSA, CryptoUtil.encodeBase64(inserter.getDsaPublicKey()));
//        userElement.addAttribute(PUBLIC_DH, CryptoUtil.encodeBase64(inserter.getDhPublicKey()));
        userElement.addAttribute(ATTR_NAME, inserter.getName());

        String signature = CryptoUtil.calculateSignature(userElement, inserter.getDsaPrivateKey(), inserter
                .getDsaPublicKey());
        userElement.addAttribute(ATTR_SIGNATURE, signature);
    }

    public void writeObjects(List<AbstractSendable> sendables) {
        for (AbstractSendable object : sendables) {
            if (!object.getSender().equals(inserter.getPublicSSK())) {
                log.warn("Won't publish message from sender " + object.getSender()
                        + " because forwarding of bomessageokmarks is not yet implemented and inserter ssk is "
                        + inserter.getPublicSSK());
                continue;

            }
            XmlMessageCodec codec = XmlMessageCodec.forType(object.getClass(), this.slotElement);
            codec.setSender(inserter.getPublicSSK());

            if (object instanceof Bookmark) {
                Bookmark b = (Bookmark) object;
                codec.writeElement(ELEMENT_URI, b.getUri());
                codec.writeElement(ELEMENT_TITLE, b.getTitle());
                codec.writeElement(ELEMENT_DESCRIPTION, b.getDescription());
                codec.writeElement(ELEMENT_RATING, b.getRating());
                codec.writeElement(ELEMENT_LAST_MODIFIED, b.getLastModified());
                codec.writeElement(ELEMENT_SIZE, b.getSize());
                codec.writeElement(ELEMENT_CONTENT_TYPE, b.getContentType());

                for (String tag : b.getTags()) {
                    codec.writeElement("tag", tag);
                }
            } else if (object instanceof Pong) {
                Pong pong = (Pong) object;
                codec.writeElement(ELEMENT_INSERT_TIME, pong.getInsertTime());
                codec.writeElement(ELEMENT_PING_INSERT_TIME, pong.getPingInsertTime());
                codec.writeElement(ELEMENT_PING_FETCH_TIME, pong.getPingFetchTime());
                codec.writeElement(ELEMENT_PING_ID, pong.getPingId());
            } else if (object instanceof Ping) {
                Ping p = (Ping) object;
                codec.writeElement(ELEMENT_INSERT_TIME, p.getInsertTime());
            } else if (object instanceof Channel) {
                Channel channel = (Channel) object;
                codec.writeElement(ELEMENT_BASENAME, channel.getBasename());
                codec.writeElement(ELEMENT_LAST_SLOT, channel.getLastSlot());
                codec.writeElement(ELEMENT_LAST_INSERT_TIME, channel.getLastInsertTime());
                codec.writeElement(ELEMENT_INSERT_INTERVAL, channel.getInsertInterval());
                codec.writeElement(ELEMENT_LAST_MODIFIED, channel.getLastModified());

            }

            codec.sign(inserter);
        }
    }

    public byte[] toByteArray() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlUtils.printDocument(this.slotDocument, baos, false, false);
        return baos.toByteArray();
    }

}
