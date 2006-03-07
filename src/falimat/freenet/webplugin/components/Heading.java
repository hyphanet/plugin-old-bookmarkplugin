package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class Heading extends AbstractHtmlComponent {

    private int level = 3;

    private String content;

    public Heading(int level, String content) {
        this.level = level;
        this.content = content;
    }
    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        out.append("<h" + level + ">");
        out.append(this.content);
        out.append("</h" + level + ">");
    }

}
