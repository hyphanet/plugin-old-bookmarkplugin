package falimat.freenet.webplugin;

import java.io.PrintWriter;
import java.io.Writer;

public class HtmlWriter extends PrintWriter {

    static final String ACTION = "action";

    private HtmlPage pageBeingWritten;

    public HtmlWriter(HtmlPage page, Writer out) {
        super(out);
        this.pageBeingWritten = page;
    }

    public void writeTooltipItem(String tooltip, String content, String clazz) {
        this.write("<span title=\"" + tooltip + "\"");
        if (clazz!=null && clazz.length()!=0) {
            this.write(" class=\""+clazz+"\"");
        }
        this.write(">");
        if (content!=null) {
            this.write(content);
        }
        this.write("</span>");
    }

    public void writeActionLink(HtmlPage targetPage, String linkText, Action action, String tooltip) {

        if (targetPage == null) {
            String msg = "writeLink() can only be called after the component was added to a page";
            throw new RuntimeException(msg);
        }

        StringBuffer relativeUrl = getRelativeUrl(targetPage);

        if (action != null) {
            relativeUrl.append("?" + ACTION + "=");
            relativeUrl.append(action.getId());
        }

        this.writeLink(relativeUrl.toString(), linkText, tooltip);
    }

    public void writeCssLink(String absolutePath) {
        String relativeUrl = getRelativeUrl(absolutePath).toString();
        super.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        super.write(relativeUrl);
        super.write("\">");
    }

    // rel="stylesheet" type="text/css"
    // href="http://www.spiegel.de/css/0,5459,PB64-dmVyPWxvdyZyZXY9MjAwNjAxMjcxNzU5MTUmc3R5bGU9,00.css">
    private StringBuffer getRelativeUrl(HtmlPage targetPage) {
        return this.getRelativeUrl(targetPage.getPath());
    }

    private StringBuffer getRelativeUrl(String absoluteUrl) {
        StringBuffer relativeUrl = new StringBuffer();
        relativeUrl.append(".");
        for (int i = 0; i < pageBeingWritten.getLevel(); i++) {
            relativeUrl.append("/..");
        }
        relativeUrl.append(absoluteUrl);
        return relativeUrl;
    }

    public void writeActionLink(HtmlPage targetPage, Action action) {
        this.writeActionLink(targetPage, targetPage.getName(), action, null);
    }

    public void writeLink(HtmlPage targetPage) {
        this.writeActionLink(targetPage, null);
    }

    public void writeLink(CharSequence href, String linktext, String tooltip) {
        super.write("<a href=\"" + href + "\"");

        if (tooltip != null) {
            super.write(" title=\"" + tooltip + "\"");
        }
        super.write(">");

        super.write(linktext);
        super.write("</a>");
    }

    public void writeInput(String type, String name, String value) {
        this.writeInput(type, name, null, value);
    }

    public void writeInput(String type, String name, String id, String value) {
        super.write("<input type=\"" + type + "\" name=\"" + name + "\" class=\"" + type + "\"");
        if (id != null && id.length() > 0) {
            super.write(" id=\"" + id + "\"");
        }
        if (value != null && value.length() > 0) {
            super.write(" value=\"" + value + "\"");
        }
        super.write(">");

    }

    public void writeLabel(String id, String label) {
        super.write("<label for=\"" + id + "\">" + label + "</label>");
    }

    public void beginSpan(String clazz) {
        this.beginTag("span", clazz);
    }

    public void endSpan() {
        this.endTag("span");
    }

    public void beginDiv(String clazz) {
        this.beginTag("div", clazz);
    }

    public void endDiv() {
        this.endTag("div");
    }

    public void newLine() {
        super.append("<br>");
    }

    public void beginTag(String name, String clazz) {
        super.append("<" + name + " class=\"" + clazz + "\">");
    }

    public void endTag(String name) {
        super.append("</" + name + ">");
    }

    public void writeStacktrace(Exception e, boolean wrapInComment) {
        super.append("\n\n");
        if (wrapInComment) {
            super.append("<!--");
        }
        super.append(HtmlPage.getStacktrace(e));
        if (wrapInComment) {
            super.append("-->");
        }
        super.append("\n\n");
    }

    public void beginForm(HtmlPage targetPage) {
        super.append("<form action=\"");
        super.append(getRelativeUrl(targetPage));
        super.append("\" method=\"get\"");
        super.append(">");
    }

    public void endForm() {
        this.endTag("form");
    }

}
