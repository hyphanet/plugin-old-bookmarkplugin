package falimat.freenet.bookmarkplugin;

import java.net.MalformedURLException;

import xomat.util.ParamException;
import xomat.util.ParamRuntimeException;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.crypt.CryptoUtil;

public class LoginCredentials {

    private final static LoginCredentials instance = new LoginCredentials();

    private User currentUser;

    private LoginCredentials() {

    }

    public static LoginCredentials instance() {
        return instance;
    }

    public void login(String publicSSK, String privateSSK) throws InvalidLoginException {
        try {
            privateSSK = CryptoUtil.convertSSKString(privateSSK, true, false);
            publicSSK = CryptoUtil.convertSSKString(publicSSK, true, false);

            User user = Store.instance().getUser(publicSSK);
            boolean keypairCorrect = CryptoUtil.verifyKeypair(publicSSK, privateSSK);
            if (!keypairCorrect) {
                String msg = "This is not the valid private key for the provided public key";
                throw new InvalidLoginException(msg);
            }
            if (user != null) {
                CryptoUtil.calculateKeys(privateSSK, user);
            } else {
                user = new User(null, privateSSK);
            }
            this.currentUser = user;
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = "The was an unexpected exception during login.";
            throw new ParamRuntimeException(msg, e);
        } catch (InvalidLoginException e) {
            String msg = "Failed to login as user {0}.";
            throw new InvalidLoginException(msg, publicSSK, e);
        } catch (MalformedURLException e) {
            String msg = "This is nt a valid SSK keypair.";
            throw new InvalidLoginException(msg, e); 
        }

    }

    public class InvalidLoginException extends ParamException {

        public InvalidLoginException(String msg, String param, Exception e) {
            super(msg, param, e);
        }

        public InvalidLoginException(String msg) {
            super(msg);
        }

        public InvalidLoginException(String msg, Exception e) {
            super(msg, e);
        }


    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

}
