package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class MessageArea extends AbstractHtmlComponent {

    private String headline;

    private String message;

    private Exception exception;

    private boolean hidden;
    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {

        if (this.hidden) {
            return;
        }
        out.beginDiv("message_area");

        if (this.headline != null && this.headline.length() > 0) {
            out.beginDiv("headline");
            out.append(this.headline);
            out.endDiv();
        }

        if (this.message != null && this.message.length() > 0) {
            out.beginDiv("message");
            out.append(message);
            out.endDiv();
        }

        out.append("</div>");

        if (this.exception != null) {
            out.writeStacktrace(this.exception, true);
            this.exception = null;
        }
    }

    protected String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected String getHeadline() {
        return this.headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void showStackTrace(Exception e) {
        this.exception = e;
    }

    public void show(String headline, String message) {
        this.headline = headline;
        this.message = message;
        this.hidden = false;
    }

    public void hide() {
        this.hidden = true;
    }

}
