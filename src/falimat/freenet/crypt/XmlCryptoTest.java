package falimat.freenet.crypt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;

import junit.framework.TestCase;

import net.i2p.util.NativeBigInteger;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import xomat.util.xml.XmlUtils;
import falimat.freenet.bookmarkplugin.components.BookmarkEditor;
import falimat.freenet.bookmarkplugin.model.AbstractSendable;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.network.SlotReader;
import falimat.freenet.network.SlotWriter;
import falimat.freenet.network.XmlSlotReader;
import falimat.freenet.network.XmlSlotWriter;
import freenet.crypt.BlockCipher;
import freenet.crypt.DSA;
import freenet.crypt.DSAPrivateKey;
import freenet.crypt.DSAPublicKey;
import freenet.crypt.DSASignature;
import freenet.crypt.DiffieHellman;
import freenet.crypt.DiffieHellmanContext;
import freenet.crypt.Global;
import freenet.crypt.RandomSource;
import freenet.crypt.UnsupportedCipherException;
import freenet.crypt.Yarrow;
import freenet.crypt.ciphers.Rijndael;
import freenet.keys.InsertableClientSSK;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class XmlCryptoTest extends TestCase {

    private static final int TEST_RUNS = 10;

    private static RandomSource random = new Yarrow();

    public void testDetail() throws Exception {

        for (int i = 0; i < TEST_RUNS; i++) {
            Element element = DocumentFactory.getInstance().createElement("test");

            InsertableClientSSK clientSSK = InsertableClientSSK.createRandom(random);

            BigInteger m = CryptoUtil.calculateSHA256Digest(element);

            DSAPrivateKey privateKey = clientSSK.privKey;
            assertTrue(Arrays.equals(privateKey.asBytes(), privateKey.asBytes()));

            DSAPublicKey publicKey = clientSSK.getPubKey();
            assertTrue(CryptoUtil.verifyPublicKey(clientSSK.getURI().toString(), publicKey.getY().toByteArray()));

            DSASignature signature = DSA.sign(Global.DSAgroupBigA, privateKey, m, random);
            assertTrue(DSA.verify(publicKey, signature, m));

            byte[] privateKeyBytes = privateKey.getX().toByteArray();

            DSAPrivateKey newKey = new DSAPrivateKey(new NativeBigInteger(1, privateKeyBytes));
            assertTrue(Arrays.equals(privateKey.asBytes(), newKey.asBytes()));

            byte[] publicKeyBytes = publicKey.getY().toByteArray();
            DSAPublicKey newPublic = new DSAPublicKey(Global.DSAgroupBigA, new NativeBigInteger(1, publicKeyBytes));
            assertTrue(Arrays.equals(publicKey.asBytes(), newPublic.asBytes()));

            assertTrue(CryptoUtil.verifyPublicKey(clientSSK.getURI().toString(), publicKeyBytes));

            byte[] aotherBeytes = publicKey.asBytes();
            DSAPublicKey otherPublic = new DSAPublicKey(aotherBeytes);
            assertTrue(Arrays.equals(publicKey.asBytes(), otherPublic.asBytes()));

            assertTrue(DSA.verify(newPublic, signature, m));
        }

    }

    public void testSignature() throws Exception {

        for (int i = 0; i < TEST_RUNS; i++) {
            User alice = new User("alice", random);

            Document doc = XmlUtils.createEmptyDocument("root");
            Element root = doc.getRootElement();
            Element abc = root.addElement("abcd");
            Element def = root.addElement("def");

            // create random SSK keypair
            InsertableClientSSK clientSSK = InsertableClientSSK.createRandom(random);

            // calculate and verify string repersentation of public key
            assertTrue(CryptoUtil.verifyPublicKey(alice.getPublicSSK(), alice.getDsaPublicKey()));

            // calculate and verify signature
            String signatureString = CryptoUtil.calculateSignature(root, alice.getDsaPrivateKey(), alice
                    .getDsaPublicKey());
            assertTrue(CryptoUtil.verifySignature(root, signatureString, alice.getDsaPublicKey()));

            // check that altering the element will make verification fail
            abc.addAttribute("test", "t");
            assertFalse(CryptoUtil.verifySignature(root, signatureString, alice.getDsaPublicKey()));
        }
    }

    public void testEncrypt() throws Exception {
        for (int i = 0; i < TEST_RUNS; i++) {
            Document doc = XmlUtils.createEmptyDocument("root");
            Element root = doc.getRootElement();
            Element unencrypted = root.addElement("unencrypted");
            unencrypted.setText("This text is not encrypted");
            Element encrypted = root.addElement("encrypted");
            encrypted.setText("This text is encrypted");
            encrypted.addAttribute("attr", "This attribute is encrypted!!!");

            String originalXmlString = XmlUtils.toString(root);

            // calculate random encryption key
            String rijndaelKeyString = CryptoUtil.randomRijndaelKeystring(random);

            // encrypt the element
            CryptoUtil.encrypt(encrypted, rijndaelKeyString);

            // decrypt the element
            CryptoUtil.decrypt(encrypted, rijndaelKeyString);

            String decryptedXmlString = XmlUtils.toString(root);

            // compare that the element is equal by string comparison
            assertEquals(originalXmlString, decryptedXmlString);

        }
    }

    public void testKeyExchange() throws Exception {

        for (int i = 0; i < TEST_RUNS; i++) {

            User alice = new User("alice", random);
            User bob = new User("bob", random);

            String rijndaelKeyString = CryptoUtil.randomRijndaelKeystring(random);

            String encryptedKeyString = CryptoUtil.encryptSessionKey(rijndaelKeyString, alice.getDhPrivateKey(), bob
                    .getDhPublicKey());

            String decryptedKeyString = CryptoUtil.decryptSessionKey(encryptedKeyString, bob.getDhPrivateKey(), alice
                    .getDhPublicKey());

            if (!rijndaelKeyString.equals(decryptedKeyString)) {
                System.err.println("alice=" + alice.getPrivateSSK());
                System.err.println("bob=" + bob.getPrivateSSK());
                System.err.println("rijndaelKey=" + rijndaelKeyString);
                System.err.println("encryptedKey=" + encryptedKeyString);
                System.err.println("decryptedKey=" + decryptedKeyString);
                throw new RuntimeException();
            }
        }
    }

    public void testMessageSigning() throws Exception {
        for (int i = 0; i < TEST_RUNS; i++) {
            InsertableClientSSK aliceSSK = InsertableClientSSK.createRandom(random);
            User alice = new User("alice", aliceSSK.getInsertURI().toString());

            Bookmark bookmark = new Bookmark();
            bookmark.setUri("KSK@gpl.txt");
            bookmark.setTitle("The GNU General Public License");
            bookmark.setSender(alice.getPublicSSK());
            bookmark.setDescription("This is a copy of the gnu public license");
            bookmark.setSize(21001);
            bookmark.setContentType("text/plain");
            bookmark.setTags(BookmarkEditor.tagsAsSet("legal software freedom"));
            bookmark.setLastModified(System.currentTimeMillis());

            SlotWriter writer = new XmlSlotWriter();
            writer.setKeypair(alice);

            java.util.List<AbstractSendable> bookmarks = new LinkedList<AbstractSendable>();
            bookmarks.add(bookmark);
 
            writer.writeObjects(bookmarks);

            byte[] slotBytes = writer.toByteArray();
            System.out.print(new String(slotBytes, XmlUtils.UTF_8));

            InsertableClientSSK bobSSK = InsertableClientSSK.createRandom(random);
            String bobPublicSSK = CryptoUtil.convertSSKString(bobSSK.getURI().toString(), true, false);
            String bobPrivateSSK = CryptoUtil.convertSSKString(bobSSK.getInsertURI().toString(), true, false);

            SlotReader reader = new XmlSlotReader();
            reader.setKeypair(bobPrivateSSK, bobPublicSSK);
            reader.readMessages(slotBytes);

            java.util.List<Bookmark> retrievedBookmarks = reader.getBookmarks();

        }
    }

    public void testDebugHKeyExchange() throws IllegalBase64Exception, UnsupportedCipherException {
        DiffieHellman.init(random);
        
        for (int i = 0; i < TEST_RUNS; i++) {

            DiffieHellmanContext aliceContext = DiffieHellman.generateContext();
            DiffieHellmanContext bobContext = DiffieHellman.generateContext();

            aliceContext.setOtherSideExponential(bobContext.getOurExponential());
            bobContext.setOtherSideExponential(aliceContext.getOurExponential());

            byte[] aliceKey = aliceContext.getKey();
            byte[] bobKey = bobContext.getKey();

            assertTrue(Arrays.equals(aliceKey, bobKey));

            String rijndaelKeyString = CryptoUtil.randomRijndaelKeystring(random);

            byte[] secretSharedKey = Base64.decode(rijndaelKeyString);
            Rijndael rijndael = new Rijndael(aliceKey.length * 8);
            byte[] encodedSharedKey = CryptoUtil.encipherBytes(aliceKey, secretSharedKey, rijndael);

            BlockCipher bobCipher = new Rijndael(bobKey.length * 8);
            byte[] decryptedSharedKey = CryptoUtil.decipherBytes(bobKey, encodedSharedKey, bobCipher, false);
            String deryptedKeyString = Base64.encode(decryptedSharedKey);

            assertEquals(rijndaelKeyString, deryptedKeyString);
        }
    }
}
