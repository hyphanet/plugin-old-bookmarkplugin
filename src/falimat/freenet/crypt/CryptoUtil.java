package falimat.freenet.crypt;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.util.Arrays;

import net.i2p.util.NativeBigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import xomat.util.ParamRuntimeException;
import xomat.util.xml.XmlUtils;
import falimat.freenet.bookmarkplugin.model.User;
import freenet.crypt.BlockCipher;
import freenet.crypt.DSA;
import freenet.crypt.DSAPrivateKey;
import freenet.crypt.DSAPublicKey;
import freenet.crypt.DSASignature;
import freenet.crypt.DiffieHellmanContext;
import freenet.crypt.Digest;
import freenet.crypt.Global;
import freenet.crypt.RandomSource;
import freenet.crypt.SHA256;
import freenet.crypt.UnsupportedCipherException;
import freenet.crypt.Yarrow;
import freenet.crypt.ciphers.Rijndael;
import freenet.keys.FreenetURI;
import freenet.keys.InsertableClientSSK;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class CryptoUtil {

    private static final Log log = LogFactory.getLog(CryptoUtil.class);

    private static boolean verifyCreatedSignatures = true;

    public static DSAPublicKey getDSAPublicKey(byte[] yAsBytes) {
        BigInteger y = new NativeBigInteger(1, yAsBytes);
        DSAPublicKey pubKey = new DSAPublicKey(Global.DSAgroupBigA, y);
        return pubKey;
    }

    public static DSAPrivateKey getDSAPrivateKey(byte[] xAsBytes) {
        BigInteger x = new NativeBigInteger(1, xAsBytes);
        DSAPrivateKey key = new DSAPrivateKey(x);
        return key;
    }

    public static boolean verifySignature(Element element, String signatureString, byte[] dsaPublicKey) {
        try {
            // recreate public key and parse signature
            DSAPublicKey pubKey = getDSAPublicKey(dsaPublicKey);
            DSASignature signature = parseSignatureString(signatureString);

            // calculate a SHA256 digest for the given element
            BigInteger elementDigest = calculateSHA256Digest(element);

            // verify the signature of that digest against the public key
            boolean verified = DSA.verify(pubKey, signature, elementDigest);
            return verified;
        } catch (Exception e) {
            String msg = "Failed to verify signature {0} for element {1}";
            throw new ParamRuntimeException(msg, signatureString, XmlUtils.toString(element), e);
        }
    }

    protected static InsertableClientSSK createInsertableClientSSK(String privateSSK) throws MalformedURLException {
        
        try {
            // append arbitrary document name if privateSSK so that it can be
            // passed to constructor FreenetURI(String)
            privateSSK = convertSSKString(privateSSK, true, true);
    
            // create InsertableClientSSK to easily get the parsed private key
            // and public key
            InsertableClientSSK clientSSK = InsertableClientSSK.create((new FreenetURI(privateSSK)));
    
            return clientSSK;
        } catch (RuntimeException e) {
            String msg = "The private SSK does not seem to be a valid Freenet insert URI";
            throw new ParamRuntimeException(msg);
        }
    }

    public static String calculateSignature(Element element, byte[] dsaPrivateKey, byte[] dsaPublicKey) {
        try {

            // calculate a SHA256 digest for the given element
            BigInteger elementDigest = calculateSHA256Digest(element);

            DSAPrivateKey privateKey = getDSAPrivateKey(dsaPrivateKey);
            // sign this digest with the private key and convert the signature
            // to string
            DSASignature signature = DSA.sign(Global.DSAgroupBigA, privateKey, elementDigest, new Yarrow());
            String signatureString = createSignatureString(signature);

            // for debugging: make sure the signature was correctly calculated
            if (verifyCreatedSignatures) {
                if (!verifySignature(element, signatureString, dsaPublicKey)) {
                    String msg = "The signature {0} calculated for element {1} falied to be verified";
                    throw new ParamRuntimeException(msg, signatureString, XmlUtils.toString(element));
                }
            }

            return signatureString;
        } catch (Exception e) {
            String msg = "Failed to sign element {0}";
            throw new ParamRuntimeException(msg, XmlUtils.toString(element), e);
        }
    }

    public static boolean verifyPublicKey(String publicSSK, byte[] yAsBytes) {
        try {

            DSAPublicKey publicKey = getDSAPublicKey(yAsBytes);

            // extract the expected public key hash from the publicSSK
            publicSSK = convertSSKString(publicSSK, false, false);
            String publicKeyHashString = publicSSK.substring(0, publicSSK.indexOf(','));
            // decode the public key hash
            byte[] expectedHash = Base64.decode(publicKeyHashString);

            // calculate the real has of the public key
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(publicKey.asBytes());
            byte[] realHash = md.digest();

            // verify that is the same as the one passed
            return (Arrays.equals(expectedHash, realHash));
        } catch (Exception e) {
            String msg = "Failed to verify hash {0} of public key {1}";
            throw new ParamRuntimeException(msg, publicSSK, encodeBase64(yAsBytes), e);
        }
    }

    public static BigInteger calculateSHA256Digest(Element element) {
        // TODO: Do something more standardized to serialize XML (maybe use
        // DOMHASH)
        byte[] bytes = XmlUtils.elementAsByteArray(element);
        Digest sha1 = new SHA256();
        sha1.update(bytes);
        byte[] hash = sha1.digest();
        return new BigInteger(1, hash);
    }

    private static DSASignature parseSignatureString(String signatureString) throws IllegalBase64Exception {
        String rString = signatureString.substring(0, signatureString.indexOf(','));
        String sString = signatureString.substring(signatureString.indexOf(',') + 1);
        BigInteger rBigInt = new BigInteger(1, Base64.decode(rString));
        BigInteger sBigInt = new BigInteger(1, Base64.decode(sString));
        DSASignature signature = new DSASignature(rBigInt, sBigInt);
        return signature;
    }

    private static String createSignatureString(DSASignature signature) {
        String r = Base64.encode(signature.getR().toByteArray());
        String s = Base64.encode(signature.getS().toByteArray());
        return r + "," + s;
    }

    public static String convertSSKString(String sskString, boolean shouldIncludeKeytype, boolean mustHaveDocname) {
        // strip a leading "freenet:" prefix
        if (sskString.startsWith("freenet:")) {
            sskString = sskString.substring(sskString.indexOf(":") + 1);
        }
        // append arbitrary document name if the sskString doesn't have it
        if (mustHaveDocname) {
            if (!sskString.contains("/")) {
                sskString = sskString + "/";
            }
            if (sskString.endsWith("/")) {
                sskString += "0";
            }
        } else {
            if (sskString.contains("/")) {
                sskString = sskString.substring(0, sskString.indexOf("/"));
            }
        }
        // strip or insert SSK@ prefix
        if (shouldIncludeKeytype) {
            if (!sskString.startsWith("SSK@")) {
                sskString = "SSK@" + sskString;
            }
        } else {
            if (sskString.startsWith("SSK@")) {
                sskString = sskString.substring(sskString.indexOf("@") + 1);
            }
        }
        return sskString;
    }

    public static byte[] calculateDHExponential(byte[] dsaPrivateKey) {
        try {
            DiffieHellmanContext dhContext = getContext(dsaPrivateKey);

            return dhContext.getOurExponential().toByteArray();
        } catch (Exception e) {
            String msg = "Failed to create DiffieHellman exponential from private key {0}";
            throw new ParamRuntimeException(msg, encodeBase64(dsaPrivateKey), e);
        }
    }

    private static DiffieHellmanContext getContext(byte[] dsaPrivateKey) {
        try {
            BigInteger x = new BigInteger(1, dsaPrivateKey);
            BigInteger X = Global.DHgroupA.g.modPow(x, Global.DHgroupA.p);

            return new DiffieHellmanContext(new NativeBigInteger(x), new NativeBigInteger(X), Global.DHgroupA);
        } catch (Exception e) {
            String msg = "Failed to create DiffieHellman exponential from private key {0}";
            throw new ParamRuntimeException(msg, Base64.encode(dsaPrivateKey), e);
        }
    }

    public static String encryptSessionKey(String rijndaelKeyString, byte[] senderExponent, byte[] recipientExponential) {
        try {
            DiffieHellmanContext aliceContext = getContext(senderExponent);
            NativeBigInteger Y = new NativeBigInteger(1, recipientExponential);
            aliceContext.setOtherSideExponential(Y);
            byte[] aliceKey = aliceContext.getKey();
            if (log.isDebugEnabled()) {
                log.debug("aliceKey has length of " + aliceKey.length);
            }

            byte[] secretSharedKey = Base64.decode(rijndaelKeyString);
            if (log.isDebugEnabled()) {
                log.debug("secretSharedKey has length of " + secretSharedKey.length);
            }

            BlockCipher cipher = new Rijndael(aliceKey.length * 8);
            byte[] encodedSharedKey = encipherBytes(aliceKey, secretSharedKey, cipher);
            if (log.isDebugEnabled()) {
                log.debug("encodedSharedKey has length of " + encodedSharedKey.length);
            }
            
            String encodedKeyString = Base64.encode(encodedSharedKey);
            return encodedKeyString;
        } catch (Exception e) {
            String msg = "Failed to encrypt shared rijndael key {0} for recpient {1}";
            throw new ParamRuntimeException(msg, rijndaelKeyString, recipientExponential, e);
        }
    }

    public static String decryptSessionKey(String encryptedKeyString, byte[] recipientExponent, byte[] senderExponential) {
        try {
            DiffieHellmanContext myContext = getContext(recipientExponent);
            NativeBigInteger Y = new NativeBigInteger(1, senderExponential);
            myContext.setOtherSideExponential(Y);
            byte[] bobKey = myContext.getKey();
            if (log.isDebugEnabled()) {
                log.debug("bobKey has length of " + bobKey.length);
            }
            
            byte[] encryptedSharedKey = Base64.decode(encryptedKeyString);
            if (log.isDebugEnabled()) {
                log.debug("encryptedSharedKey has length of " + encryptedSharedKey.length);
            }
            
            BlockCipher cipher = new Rijndael(bobKey.length * 8);
            byte[] decryptedSharedKey = decipherBytes(bobKey, encryptedSharedKey, cipher, false);
            if (log.isDebugEnabled()) {
                log.debug("decryptedSharedKey has length of " + decryptedSharedKey.length);
            }
            
            String decryptedKeyString = Base64.encode(decryptedSharedKey);
            return decryptedKeyString;

        } catch (Exception e) {
            String msg = "Failed to encrypt shared rijndael key {0} from sender {1}";
            throw new ParamRuntimeException(msg, encryptedKeyString, senderExponential, e);
        }
    }

    public static void encrypt(Element toBeEncrypted, String rijndaelKeyString) {
        try {

            // decode rjndael key and serialize xml element
            byte[] keyBytes = Base64.decode(rijndaelKeyString);
            byte[] contentBytes = XmlUtils.elementAsByteArray(toBeEncrypted);

            Rijndael rijndael = new Rijndael(keyBytes.length * 8);
            rijndael.initialize(keyBytes);

            // encipher the byte array
            byte[] encryptedBytes = encipherBytes(keyBytes, contentBytes, rijndael);

            // convert encrypted byte array to string
            String encodedString = Base64.encode(encryptedBytes);

            // replace content of element with encrypted string
            toBeEncrypted.clearContent();
            toBeEncrypted.attributes().clear();
            toBeEncrypted.setText(encodedString);
        } catch (Exception e) {
            String msg = "Failed to encrypt element {0} with key {1}";
            throw new ParamRuntimeException(msg, XmlUtils.toString(toBeEncrypted), rijndaelKeyString, e);
        }
    }

    public static void decrypt(Element encrypted, String rijndaelKeyString) {
        try {
            // decode element content and rijndael key
            byte[] encryptedBytes = Base64.decode(encrypted.getText());
            byte[] keyBytes = Base64.decode(rijndaelKeyString);

            // decipher the bytes
            Rijndael rijndael = new Rijndael(keyBytes.length * 8);
            rijndael.initialize(keyBytes);
            byte[] contentBytes = decipherBytes(keyBytes, encryptedBytes, rijndael, true);

            // create xml element from deciphered bytes
            Element decrypted = XmlUtils.elementFromByteArray(contentBytes);

            // replace content in encrypted element with the decrypted content
            encrypted.clearContent();
            encrypted.setAttributes(decrypted.attributes());
            encrypted.setContent(decrypted.content());

        } catch (Exception e) {
            String msg = "Failed to decrypt element from string {0}";
            throw new ParamRuntimeException(msg, encrypted.getText(), e);
        }
    }

    static byte[] encipherBytes(byte[] keyBytes, byte[] contentBytes, BlockCipher rijndael)
            throws UnsupportedCipherException {

        rijndael.initialize(keyBytes);
        byte[] paddedBytes = padByteArray(contentBytes, rijndael.getBlockSize());
        byte[] encryptedBytes = new byte[paddedBytes.length];

        int blockLength = rijndael.getBlockSize() / 8;
        byte[] plainBlock = new byte[blockLength];
        byte[] cipherBlock = new byte[blockLength];
        for (int i = 0; i < encryptedBytes.length / blockLength; i++) {
            System.arraycopy(paddedBytes, i * blockLength, plainBlock, 0, blockLength);
            rijndael.encipher(plainBlock, cipherBlock);
            System.arraycopy(cipherBlock, 0, encryptedBytes, i * blockLength, blockLength);
        }
        return encryptedBytes;
    }

    static byte[] decipherBytes(byte[] keyBytes, byte[] encryptedBytes, BlockCipher blockCipher, boolean removePadding)
            throws UnsupportedCipherException {

        blockCipher.initialize(keyBytes);
        byte[] decipheredBytes = new byte[encryptedBytes.length];

        int blockLength = blockCipher.getBlockSize() / 8;
        byte[] plainBlock = new byte[blockLength];
        byte[] cipherBlock = new byte[blockLength];
        for (int i = 0; i < encryptedBytes.length / blockLength; i++) {
            System.arraycopy(encryptedBytes, i * blockLength, cipherBlock, 0, blockLength);
            blockCipher.decipher(cipherBlock, plainBlock);
            System.arraycopy(plainBlock, 0, decipheredBytes, i * blockLength, blockLength);
        }

        if (removePadding) {
            return removePadding(decipheredBytes, blockCipher.getBlockSize());
        } else {
            return decipheredBytes;
        }
    }

    public static byte[] padByteArray(byte[] bytes, int blockSize) {
        int additionalBytes = bytes.length % (blockSize / 8);
        byte[] out;
        if (additionalBytes == 0) {
            out = new byte[bytes.length];
        } else {
            out = new byte[bytes.length + (blockSize / 8 - additionalBytes)];
        }
        System.arraycopy(bytes, 0, out, 0, bytes.length);
        if (additionalBytes > 0) {
            Arrays.fill(out, bytes.length, out.length, (byte) 0);
        }
        if (log.isDebugEnabled()) {
            log.debug("padded '" + new String(bytes) + "' to '" + new String(out) + "' with length " + out.length);
        }
        return out;
    }

    public static byte[] removePadding(byte[] bytes, int blockSize) {

        int firstPossiblePadByte = bytes.length - blockSize / 8;
        int firstPadByte = 0;
        for (int i = firstPossiblePadByte; i < bytes.length; i++) {
            if (bytes[i] == (byte) 0) {
                firstPadByte = i;
                break;
            }
        }
        if (firstPadByte == 0) {
            firstPadByte = bytes.length;
        }
        byte[] out = new byte[firstPadByte];
        System.arraycopy(bytes, 0, out, 0, firstPadByte);
        if (log.isDebugEnabled()) {
            log.debug("unpadded '" + new String(bytes) + "' to '" + new String(out) + "'");
        }
        return out;
    }

    public final static String randomRijndaelKeystring(RandomSource random) {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        String rijndaelKeyString = Base64.encode(randomBytes);
        return rijndaelKeyString;
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.encode(bytes);
    }

    public static void calculateKeys(String privateSSK, User user) throws MalformedURLException {
        InsertableClientSSK clientSSK = createInsertableClientSSK(privateSSK);

        DSAPrivateKey privateKey = clientSSK.privKey;
        DSAPublicKey publicKey = clientSSK.getPubKey();

        String publicSSK = convertSSKString(clientSSK.getURI().toString(), true, false);
        privateSSK = convertSSKString(clientSSK.getInsertURI().toString(), true, false);
        byte[] dsaPrivateKey = privateKey.getX().toByteArray();
        byte[] dsaPublicKey = publicKey.getY().toByteArray();
        byte[] dhPublicKey = CryptoUtil.calculateDHExponential(dsaPrivateKey);

        user.calculateKeys(publicSSK, dsaPrivateKey, dsaPublicKey, dhPublicKey, privateSSK);
    }

    public static byte[] decodeBase64(String string) throws IllegalBase64Exception {
        return Base64.decode(string);
    }

    public static boolean verifyKeypair(String publicSSK, String privateSSK) {
        try {
            User user = new User();
            calculateKeys(privateSSK, user);
            return (user.getPublicSSK().equals(publicSSK));
        } catch (MalformedURLException e) {
            return false;
        }
    }



}
