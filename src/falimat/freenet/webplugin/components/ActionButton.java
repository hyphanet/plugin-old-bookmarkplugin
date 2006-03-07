package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.Action;
import falimat.freenet.webplugin.ActionComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class ActionButton extends AbstractHtmlComponent implements ActionComponent {

    private HtmlPage targetPage;

    private Action action;

    private String text;
    
    private String tooltip;

    public ActionButton(String text) {
        this.text = text;
    }

    public ActionButton(String text, String tooltip) {
        this.text = text;
        this.tooltip = tooltip;
    }

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {

        if (this.targetPage == null) {
            this.targetPage = contextPage;
        }
        out.beginDiv("button");

        out.writeActionLink(targetPage, this.text, this.action, this.tooltip);

        out.endDiv();
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction(String id) {
       if (this.action.getId().equals(id)) {
           return this.action;
       }
       return null;
    }


}
