package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class NavigationMenu extends AbstractHtmlComponent  {

    private final HtmlPage rootPage;
    
    public NavigationMenu(HtmlPage navigationRootPage) {
        this.rootPage = navigationRootPage;
    }
    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
      out.beginDiv("nav");
      
      out.append("<div class=\"toplevel\">");
      out.writeLink( rootPage);
      out.append("</div>");

      this.writeMenu(out, rootPage, contextPage);
      
      

      out.endDiv();
      
    }
    
    private void writeMenu(HtmlWriter out, HtmlPage parentPage, HtmlPage contextPage) {
        HtmlPage[] subPages = parentPage.listSubpages();
        if (subPages.length==0) {
            return;
        }
        
        out.append("<ul>");
        
        for (int i=0; i<subPages.length; i++) {
            out.append("<li>");
            out.writeLink(subPages[i]);
            out.append("</li>");
        }
        
        out.append("</ul>");
        
    }
    
    

}
