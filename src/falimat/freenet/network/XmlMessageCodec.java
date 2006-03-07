package falimat.freenet.network;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;

import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.crypt.CryptoUtil;

public class XmlMessageCodec {

    private final static String ELEMENT_PLAIN = "plain";

    private final static String ELEMENT_CIPHERED = "ciphered";

    private final static String ELEMENT_SENDER = "sender";

    private final static String ELEMENT_RECIPIENT = "recipient";

    private final static String ATTR_SIGNATURE = "signature";

    private final static String ATTR_SSK = "ssk";

    private Element xmlElement;

    private Element cipheredElement;

    private Element plainElement;

    private String messageType;

    private String senderSSK;

    private Set<String> recipients = new TreeSet<String>();

    private boolean hiddenRecipients;

    private boolean hiddenSender;

    private boolean hiddenContent;

    protected XmlMessageCodec(String type, Element slotElement) {
        this.messageType = type;
        this.xmlElement = slotElement.addElement(this.messageType);
    }

    protected XmlMessageCodec(Element messageElement) {
        this.messageType = messageElement.getName();
        this.xmlElement = messageElement;

        this.plainElement = this.xmlElement.element(ELEMENT_PLAIN);
        this.cipheredElement = this.xmlElement.element(ELEMENT_CIPHERED);

        this.senderSSK = this.plainElement.element(ELEMENT_SENDER).attributeValue(ATTR_SSK);
    }
    
    static XmlMessageCodec forType(Class clazz, Element slotElement) {
        String className = clazz.getName().substring(clazz.getName().lastIndexOf('.')+1).toLowerCase();
        XmlMessageCodec codec = new XmlMessageCodec(className, slotElement);
        return codec;
    }
    protected void enableEncryption(boolean sender, boolean recipients, boolean content) {
        this.hiddenSender = sender;
        this.hiddenRecipients = recipients;
        this.hiddenContent = content;
    }

    public void setSender(String senderSSK) {
        this.senderSSK = senderSSK;
        Element senderElement;
        if (this.hiddenSender) {
            senderElement = this.getCipheredElement().addElement(ELEMENT_SENDER);

        } else {
            senderElement = this.getPlainElement().addElement(ELEMENT_SENDER);
        }

        senderElement.addAttribute(ATTR_SSK, senderSSK);
    }

    public String getSender() {
        return this.senderSSK;
    }

    protected Element getPlainElement() {
        if (this.plainElement == null) {
            this.plainElement = this.xmlElement.addElement(ELEMENT_PLAIN);
        }
        return this.plainElement;
    }

    protected Element getCipheredElement() {
        if (this.cipheredElement == null) {
            this.cipheredElement = this.xmlElement.addElement(ELEMENT_CIPHERED);
        }
        return this.cipheredElement;
    }

    public void writeElement(String name, long longValue) {
        this.writeElement(name, Long.toString(longValue));
    }

    public void writeElement(String name, int intValue) {
        this.writeElement(name, Integer.toString(intValue));
    }

    public void writeElement(String name, String textValue) {
        if (textValue == null) {
            return;
        }
        Element element;
        if (this.hiddenContent) {
            element = this.getCipheredElement().addElement(name);
        } else {
            element = this.getPlainElement().addElement(name);
        }
        element.setText(textValue);
        return;
    }

    public void sign(User sender) {
        String signature = CryptoUtil.calculateSignature(this.xmlElement, sender.getDsaPrivateKey(), sender.getDsaPublicKey());
        this.xmlElement.addAttribute(ATTR_SIGNATURE, signature);
    }

    public boolean verify(User sender) {
        String signature = this.xmlElement.attributeValue(ATTR_SIGNATURE);
        this.xmlElement.addAttribute(ATTR_SIGNATURE, null);
        boolean verified = CryptoUtil.verifySignature(this.xmlElement, signature, sender.getDsaPublicKey());
        this.xmlElement.addAttribute(ATTR_SIGNATURE, signature);
        return verified;
    }

    public String getElementText(String name) {
        if (this.hiddenContent) {
            // TODO: implement hidden content
            throw new RuntimeException("hidden content not implemented");
        } else {
            return this.plainElement.elementText(name);
        }
    }

    public int getElementInt(String name, int defaultValue) {
        String string = this.getElementText(name);
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public long getElementLong(String name, long defaultValue) {
        String string = this.getElementText(name);
        try {
            return Long.parseLong(string);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Set<String> getElementTextSet(String name) {
        Set<String> out = new TreeSet<String>();
        List<Element> elements = this.plainElement.elements(name);
        if (elements != null) {
            for (Element element : elements) {
                out.add(element.getText());
            }
        }
        return out;
    }

}
