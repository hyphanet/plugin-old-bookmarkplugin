package falimat.freenet.network;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.Element;

import xomat.util.ParamException;
import xomat.util.ParamRuntimeException;
import xomat.util.xml.ValidationException;
import xomat.util.xml.XmlUtils;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.Ping;
import falimat.freenet.bookmarkplugin.model.Pong;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.crypt.CryptoUtil;

public class XmlSlotReader implements SlotReader {

    private String privateSSK;

    private String publicSSK;

    private Document slotDocument;

    private Element slotElement;

    private Map<String, User> sskUserMap = new TreeMap<String, User>();

    private Map<String, String> sskDhMap = new TreeMap<String, String>();

    private String contentType = XmlSlotWriter.CONTENT_TYPE;

    public void setKeypair(String privateSSK, String publicSSK) {
        this.privateSSK = privateSSK;
        this.publicSSK = publicSSK;
    }

    public List<User> getUsers() {
        return new LinkedList<User>(this.sskUserMap.values());
    }

    public void readMessages(byte[] slotBytes) throws ParamException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(slotBytes);
            this.slotDocument = XmlUtils.readDocument(bais, false);
            this.slotElement = this.slotDocument.getRootElement();

            List<Element> userElements = this.slotElement.elements(XmlSlotWriter.ELEMENT_USER);
            for (Element userElement : userElements) {

                try {

                    String ssk = userElement.attributeValue(XmlSlotWriter.PUBLIC_SSK);
                    byte[] dsa = CryptoUtil.decodeBase64(userElement.attributeValue(XmlSlotWriter.PUBLIC_DSA));

                    // byte[] dh =
                    // CryptoUtil.decodeBase64(userElement.attributeValue(XmlSlotWriter.PUBLIC_DH));
                    String name = userElement.attributeValue(XmlSlotWriter.ATTR_NAME);

                    if (!CryptoUtil.verifyPublicKey(ssk, dsa)) {
                        String msg = "Failed to verify public dsa key in key element {0}";
                        throw new ParamException(msg, XmlUtils.toString(userElement));
                    }

                    String expectedSignature = userElement.attributeValue(XmlSlotWriter.ATTR_SIGNATURE);
                    userElement.addAttribute(XmlSlotWriter.ATTR_SIGNATURE, null);
                    if (!CryptoUtil.verifySignature(userElement, expectedSignature, dsa)) {
                        String msg = "Failed to verify signature of key element {0}";
                        throw new ParamException(msg, XmlUtils.toString(userElement));
                    }

                    User user = new User();
                    user.setName(name);
                    user.setPublicSSK(ssk);
                    user.setDsaPublicKey(dsa);
                    // user.setDhPublicKey(dh);

                    this.sskUserMap.put(ssk, user);
                } catch (Exception e) {
                    String msg = "Failed to parse user data from element {0}";
                    throw new ParamRuntimeException(msg, XmlUtils.toString(userElement), e);
                }
            }

        } catch (ValidationException e) {
            String msg = "XmlSlotReader failed to validate document";
            throw new ParamException(msg, e);
        }
    }

    public List<Channel> getChannels() {
        List<Channel> channels = new LinkedList<Channel>();

        List<Element> channelElement = this.slotElement.elements(XmlSlotWriter.TYPE_CHANNEL);
        for (Element cElement : channelElement) {
            XmlMessageCodec reader = new XmlMessageCodec(cElement);
            String publicSSk = reader.getSender();
            User sender = this.sskUserMap.get(publicSSk);
            if (sender == null) {
                String msg = "No user data found for verifying signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(cElement));
            }
            if (!reader.verify(sender)) {
                String msg = "Failed to verify signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(cElement));
            }
            Channel c = new Channel();
            c.setSender(sender.getPublicSSK());
            c.setBasename(reader.getElementText(XmlSlotWriter.ELEMENT_BASENAME));
            c.setLastSlot(reader.getElementInt(XmlSlotWriter.ELEMENT_LAST_SLOT, -1));
            c.setLastInsertTime(reader.getElementLong(XmlSlotWriter.ELEMENT_LAST_INSERT_TIME, -1));
            c.setInsertInterval(reader.getElementInt(XmlSlotWriter.ELEMENT_INSERT_INTERVAL, -1));
            c.setLastModified(reader.getElementLong(XmlSlotWriter.ELEMENT_LAST_MODIFIED, -1));
            channels.add(c);
        }
        return channels;
    }

    public List<Bookmark> getBookmarks() {
        List<Bookmark> bookmarks = new LinkedList<Bookmark>();

        List<Element> bookmarkElements = this.slotElement.elements(XmlSlotWriter.TYPE_BOOKMARK);
        for (Element bElement : bookmarkElements) {
            XmlMessageCodec reader = new XmlMessageCodec(bElement);
            String publicSSk = reader.getSender();
            User sender = this.sskUserMap.get(publicSSk);
            if (sender == null) {
                String msg = "No user data found for verifying signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(bElement));
            }
            if (!reader.verify(sender)) {
                String msg = "Failed to verify signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(bElement));
            }

            Bookmark b = new Bookmark();
            b.setSender(sender.getPublicSSK());
            b.setUri(reader.getElementText("uri"));
            b.setTitle(reader.getElementText("title"));
            b.setDescription(reader.getElementText("description"));
            b.setRating(reader.getElementInt("rating", -1));
            b.setLastModified(reader.getElementLong("lastModified", 0));
            b.setSize(reader.getElementLong("size", 0));
            b.setContentType(reader.getElementText("contentType"));
            b.setTags(reader.getElementTextSet("tag"));

            bookmarks.add(b);
        }
        return bookmarks;
    }

    public List<Ping> getPings() {
        List<Ping> pings = new LinkedList<Ping>();

        List<Element> pingElements = this.slotElement.elements(XmlSlotWriter.TYPE_PING);
        for (Element pElement : pingElements) {
            XmlMessageCodec reader = new XmlMessageCodec(pElement);
            String publicSSk = reader.getSender();
            User sender = this.sskUserMap.get(publicSSk);
            if (sender == null) {
                String msg = "No user data found for verifying signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(pElement));
            }
            if (!reader.verify(sender)) {
                String msg = "Failed to verify signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(pElement));
            }

            // TODO: parse recipients
            Ping p = new Ping();
            p.setSender(sender.getPublicSSK());
            p.setInsertTime(reader.getElementInt(XmlSlotWriter.ELEMENT_INSERT_TIME, -1));
            pings.add(p);
        }
        return pings;
    }

    public List<Pong> getPongs() {
        List<Pong> pongs = new LinkedList<Pong>();

        List<Element> pongElements = this.slotElement.elements(XmlSlotWriter.TYPE_PONG);
        for (Element pElement : pongElements) {
            XmlMessageCodec reader = new XmlMessageCodec(pElement);
            String publicSSk = reader.getSender();
            User sender = this.sskUserMap.get(publicSSk);
            if (sender == null) {
                String msg = "No user data found for verifying signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(pElement));
            }
            if (!reader.verify(sender)) {
                String msg = "Failed to verify signature of message {0}";
                throw new ParamRuntimeException(msg, XmlUtils.toString(pElement));
            }
            // TODO: parse recipients

            Pong p = new Pong();
            p.setSender(sender.getPublicSSK());
            p.setInsertTime(reader.getElementInt(XmlSlotWriter.ELEMENT_INSERT_TIME, -1));
            p.setPingId(reader.getElementText(XmlSlotWriter.ELEMENT_PING_ID));
            p.setPingInsertTime(reader.getElementInt(XmlSlotWriter.ELEMENT_PING_INSERT_TIME, -1));
            p.setPingFetchTime(reader.getElementInt(XmlSlotWriter.ELEMENT_PING_FETCH_TIME, -1));
            pongs.add(p);
        }
        return pongs;
    }

    public String getContentType() {
        return this.contentType;
    }

}
