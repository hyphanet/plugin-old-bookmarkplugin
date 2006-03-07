package falimat.freenet.bookmarkplugin.components;

import falimat.freenet.bookmarkplugin.LoginCredentials;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.bookmarkplugin.ui.AddBookmarkPage;
import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.Action;
import falimat.freenet.webplugin.ActionComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import falimat.freenet.webplugin.SimpleAction;
import falimat.freenet.webplugin.components.ActionButton;
import freenet.pluginmanager.HTTPRequest;

public class LoginMessage extends AbstractHtmlComponent implements ActionComponent {
    ActionButton logoutButton = new ActionButton("x", "Logout");
    
    public LoginMessage(final AddBookmarkPage addBookmarkPage) {
        
        logoutButton.setAction(new SimpleAction() {
    
            @Override
            public void execute(HTTPRequest request) {
                LoginCredentials.instance().logout();
                addBookmarkPage.onLogout();
            }
    
        });
        
    }    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        
        out.beginDiv("login_message");
        User user = LoginCredentials.instance().getCurrentUser();
        
        if (user==null) {
            out.write("You are not logged in.");
        } else {
            out.write("You are logged in as ");
            out.writeTooltipItem(user.getPublicSSK(), user.getName(), "nick");
            this.logoutButton.renderHtml(out, contextPage);            
        }
        

        
        out.endDiv();
    }
    public Action getAction(String id) {
       return this.logoutButton.getAction(id);
    }

}
