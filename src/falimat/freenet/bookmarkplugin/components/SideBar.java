package falimat.freenet.bookmarkplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import falimat.freenet.webplugin.components.NavigationMenu;

public class SideBar extends AbstractHtmlComponent {

    private NavigationMenu navigation;
    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
       out.beginDiv("sidebar");
       
       if (this.navigation!=null) {
           navigation.renderHtml(out, contextPage);
       }
       
       out.endDiv();
    }

    public NavigationMenu getNavigation() {
        return this.navigation;
    }

    public void setNavigation(NavigationMenu navigation) {
        this.navigation = navigation;
    }

}
