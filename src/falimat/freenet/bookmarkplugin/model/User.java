package falimat.freenet.bookmarkplugin.model;

import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xomat.util.ParamRuntimeException;

import falimat.freenet.crypt.CryptoUtil;
import freenet.crypt.RandomSource;
import freenet.keys.InsertableClientSSK;

public class User extends AbstractSendable{

    private final static Log log = LogFactory.getLog(User.class);

    private String name;

    private String publicSSK;
    
    private String privateSSK;

    private byte[] dsaPublicKey;

    private byte[] dsaPrivateKey;

    private byte[] dhPublicKey;

    public User(String name, String privateSSK) throws MalformedURLException {
        this.name = name;
        this.privateSSK = privateSSK;
        CryptoUtil.calculateKeys(privateSSK, this);
    }

    public User() {

    }

    public User(String name, RandomSource random) {
        try {
            this.name = name;
            this.privateSSK = CryptoUtil.convertSSKString(InsertableClientSSK.createRandom(random).getInsertURI().toString(), true, false);
            CryptoUtil.calculateKeys(this.privateSSK, this);
        } catch (MalformedURLException e) {
            String msg = ("Failed to create user {0} with new random keypair");
            throw new ParamRuntimeException(msg, name, e);
        }
    }

    public void calculateKeys(String publicSSK, byte[] dsaPrivateKey, byte[] dasPublicKey, byte[] dhPublicKey, String privateSSK) {
        this.publicSSK = publicSSK;
        this.dsaPrivateKey = dsaPrivateKey;
        this.dsaPublicKey = dasPublicKey;
        this.dhPublicKey = dhPublicKey;
        this.privateSSK = privateSSK;
    }

    public byte[] getDhPublicKey() {
        return this.dhPublicKey;
    }

    public void setDhPublicKey(byte[] dhPublicKey) {
        this.dhPublicKey = dhPublicKey;
    }

    public byte[] getDhPrivateKey() {
        return this.dsaPrivateKey;
    }

    public byte[] getDsaPrivateKey() {
        return this.dsaPrivateKey;
    }

    public byte[] getDsaPublicKey() {
        return this.dsaPublicKey;
    }

    public void setDsaPublicKey(byte[] dsaPublicKey) {
        this.dsaPublicKey = dsaPublicKey;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String nick) {
        this.name = nick;
    }

    public String getPublicSSK() {
        return this.publicSSK;
    }

    public void setPublicSSK(String publicSSK) {
        this.publicSSK = publicSSK;
    }

    public String getPrivateSSK() {
        return this.privateSSK;
    }
}
