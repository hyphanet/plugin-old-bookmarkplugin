/**
 * 
 */
package falimat.freenet.bookmarkplugin.ui;

import falimat.freenet.bookmarkplugin.BookmarkPlugin;
import falimat.freenet.bookmarkplugin.LoginCredentials;
import falimat.freenet.bookmarkplugin.LoginCredentials.InvalidLoginException;
import falimat.freenet.bookmarkplugin.components.BookmarkEditor;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.network.RegExes;
import falimat.freenet.webplugin.FormAction;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.KeyNotFoundException;
import falimat.freenet.webplugin.SimpleAction;
import falimat.freenet.webplugin.components.ActionButton;
import falimat.freenet.webplugin.components.DefinitionList;
import falimat.freenet.webplugin.components.Form;
import falimat.freenet.webplugin.components.Heading;
import falimat.freenet.webplugin.components.LabeledTextField;
import falimat.freenet.webplugin.components.MessageArea;
import falimat.freenet.webplugin.components.SubmitButton;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.crypt.Yarrow;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.PluginHTTPRequest;

public class AddBookmarkPage extends HtmlPage {
    /**
     * 
     */
    private final BookmarkPlugin bookmarkPlugin;

    public AddBookmarkPage(BookmarkPlugin plugin, String name) {
        super(name);
        this.bookmarkPlugin = plugin;
    }

    private Form addBookmarkForm;

    private LabeledTextField uriTextField;

    private SubmitButton checkButton;

    private MessageArea messageArea;

    private Bookmark bookmark;

    private LoginForm loginForm;
    
    private NewUserForm newUserForm;

    private BookmarkEditor editor = new BookmarkEditor();

    @Override
    public void constructComponents() {

        this.loginForm = new LoginForm();
        this.loginForm.constructComponents();
        this.addComponent(this.loginForm);
        
        this.newUserForm = new NewUserForm();
        this.newUserForm.constructComponents();
        this.addComponent(this.newUserForm);
        this.newUserForm.hide();

        this.addBookmarkForm = new Form();

        this.addBookmarkForm.addComponent(new Heading(2, "Create new or edit existing bookmark..."));

        this.uriTextField = new LabeledTextField("uri", "Enter URI to bookmark");
        this.uriTextField.setRequired(true);
        this.uriTextField.setValidation("This is not a valid freenet URI", RegExes.FREENET_URI);

        this.addBookmarkForm.addComponent(this.uriTextField);

        this.checkButton = new SubmitButton("Create");
        this.checkButton.setAction(new FormAction(this.addBookmarkForm) {
            @Override
            protected void onSubmit(PluginHTTPRequest request) {
                messageArea.hide();
                try {
                    bookmark = createBookmark(uriTextField.getValue());
                    if (Store.instance().containsBookmark(bookmark)) {
                        bookmark = Store.instance().loadBookmark(bookmark);
                    }
                    editor.setBookmark(bookmark);
                } catch (KeyNotFoundException e) {
                    messageArea.show("The key could not be retrieved from your local node",
                            "You can only bookmark keys that you have recently requested.");
                    messageArea.showStackTrace(e);
                } finally {
                    addBookmarkForm.resetFormValues();
                }
            }

            @Override
            protected void onInvalidSubmission(PluginHTTPRequest request) {
                messageArea.hide();
                editor.setBookmark(null);
            }
        });

        this.addBookmarkForm.addComponent(this.checkButton);
        this.addBookmarkForm.hide();

        super.addComponent(this.addBookmarkForm);

        this.messageArea = new MessageArea();
        this.messageArea.hide();

        this.addComponent(this.messageArea);

        this.addComponent(this.editor);

        ActionButton newAccountButton = new ActionButton("New Account", "Create a new account with a random keypair");
        newAccountButton.setAction(new SimpleAction() {

            @Override
            public void execute(PluginHTTPRequest request) {
                LoginCredentials.instance().logout();
                addBookmarkForm.hide();       
                loginForm.hide();
                newUserForm.show();
                newUserForm.resetFormValues();
                editor.setBookmark(null);
            }

        });
        this.addComponent(newAccountButton);        
        

    }
    
    class NewUserForm extends Form {
        
        private User user;
        
        NewUserForm() {

        }
        
        @Override
        public void show() {
            this.constructComponents();
            super.show();
        }
        
        public void constructComponents() {
            this.user = new User(null, new Yarrow());
            
            super.removeAllComponents();
            
            Heading heading = new Heading(2, "Create User Account");
            this.addComponent(heading);
            
            MessageArea area = new MessageArea();
            area.show("Your generated keypair", "This is a randomly generated keypair that you should use from now on to log into this plugin. Please store it in a safe place.");
            this.addComponent(area);
            
            DefinitionList definitionList = new DefinitionList();
            definitionList.addEntry("Public SSK", this.user.getPublicSSK());
            definitionList.addEntry("Private SSK", this.user.getPrivateSSK());
            this.addComponent(definitionList);
            
            final LabeledTextField nickTextField = new LabeledTextField("nick", "Please choose a nickname");
            nickTextField.setRequired(true);
            this.addComponent(nickTextField);
            
            final SubmitButton submitButton = new SubmitButton("Create Account");
            submitButton.setAction(new FormAction(this) {

                @Override
                protected void onSubmit(PluginHTTPRequest request) {
                    
                   String userName = nickTextField.getValue();
                   
                   User existingUser = Store.instance().getUserByName(userName);
                   if (existingUser!=null) {
                       nickTextField.showValidationMessage("There already exists an account with the nickname you chose. Please enter a unique name.");
                       return;
                   }
                   
                   user.setLastModified(System.currentTimeMillis());
                   user.setName(userName);
                   NewUserForm.this.hide();
                   loginForm.show();
                   loginForm.privateKeyField.setValue(user.getPrivateSSK());
                   loginForm.publicKeyField.setValue(user.getPublicSSK());
                   
                   Channel channel = new Channel();
                   channel.setSender(user.getPublicSSK());
                   channel.setBasename(user.getName());
                   channel.setLastInsertTime(System.currentTimeMillis());
                   
                   Store.instance().saveChannel(channel);
                   Store.instance().saveUser(user);
                }

                @Override
                protected void onInvalidSubmission(PluginHTTPRequest request) {
                    // TODO Auto-generated method stub
                    
                }
                
            });
            this.addComponent(submitButton);
            
            final SubmitButton cancelButton = new SubmitButton("Cancel");
            cancelButton.setAction(new FormAction(this, false) {

                @Override
                protected void onSubmit(PluginHTTPRequest request) {
                   NewUserForm.this.resetFormValues();
                   NewUserForm.this.hide();
                   loginForm.resetFormValues();
                   loginForm.show();
                }

                @Override
                protected void onInvalidSubmission(PluginHTTPRequest request) {
                    this.onSubmit(request);
                }
                
            });
            this.addComponent(cancelButton);            
        }
    }
    
    
    class LoginForm extends Form {
        private LabeledTextField publicKeyField;

        private LabeledTextField privateKeyField;

        private MessageArea message;

        public void constructComponents() {
            Heading heading = new Heading(2, "Login");
            this.addComponent(heading);

            message = new MessageArea();
            this.addComponent(message);

            publicKeyField = new LabeledTextField("publicKey", "Please enter your public key...");
            publicKeyField.setRequired(true);
            publicKeyField.setValidation("This is not a valid freenet SSK", RegExes.SSK_KEY);
            this.addComponent(publicKeyField);

            privateKeyField = new LabeledTextField("privateKey", "...and your private key");
            privateKeyField.setRequired(true);
            privateKeyField.setValidation("This is not a valid freenet SSK", RegExes.SSK_KEY);
            this.addComponent(privateKeyField);

            SubmitButton loginButton = new SubmitButton("Login");
            loginButton.setAction(new FormAction(this) {

                @Override
                protected void onSubmit(PluginHTTPRequest request) {
                    try {
                        LoginCredentials.instance().login(publicKeyField.getValue(), privateKeyField.getValue());
                        LoginForm.this.hide();
                        resetLoginForm();
                        addBookmarkForm.show();
                    } catch (InvalidLoginException e) {
                        privateKeyField.setValue("");
                        message.show("Invalid Login", e.getMessages());
                    }
                }

                @Override
                protected void onInvalidSubmission(PluginHTTPRequest request) {
                    LoginCredentials.instance().logout();
                    resetLoginForm();
                }

            });
            this.addComponent(loginButton);

            resetLoginForm();
        }

        private void resetLoginForm() {
            publicKeyField.setValue("");
            privateKeyField.setValue("");
            message
                    .show(
                            "How to Login:",
                            "You have to enter a valid Freenet SSK keypair here. Please beware that currently the keypair is visible in " +
                            "the text fields, as well as the location bar. Don't use a keypair you need to keep secret for this right now. ");
        }
    }

    private Bookmark createBookmark(String key) throws KeyNotFoundException {
        try {
            HighLevelSimpleClient freenetClient = this.bookmarkPlugin.obtainClient();
            freenetClient.getFetcherContext().localRequestOnly = true;
            FreenetURI uri = new FreenetURI(key);
            FetchResult fetchResult = freenetClient.fetch(uri);

            Bookmark bookmark = new Bookmark();
            bookmark.setSender(LoginCredentials.instance().getCurrentUser().getPublicSSK());
            bookmark.setContentType(fetchResult.getMimeType());
            bookmark.setSize(fetchResult.size());
            bookmark.setLastModified(System.currentTimeMillis());
            bookmark.setUri(key);
            return bookmark;
        } catch (Exception e) {
            throw new KeyNotFoundException(uriTextField.getValue());
        }
    }

    public void onLogout() {
        this.addBookmarkForm.hide();
        this.loginForm.show();
        this.editor.setBookmark(null);
        this.newUserForm.hide();

    }
}