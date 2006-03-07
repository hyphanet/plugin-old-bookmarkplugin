package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.Action;
import falimat.freenet.webplugin.ActionComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class SubmitButton extends AbstractHtmlComponent implements ActionComponent {

    private String value;

    private Action action;

    private final String name = "action";

    public SubmitButton(String value) {
        this.value = value;
    }

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
       out.writeInput("submit", this.name, this.value);
    }

    public Action getAction(String id) {
        if (value.equals(id)) {
            return this.action;
        }
        return null;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
