package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.FormComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class LabeledTextArea extends AbstractFormComponent implements FormComponent {

    private static int textFieldCount = 0;

    private String id = "txtarea_" + textFieldCount++;

    public LabeledTextArea(String name, String label) {
        this.name = name;
        this.label = label; 
    }

    @Override
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        out.writeLabel(this.id, this.label);

        out.write("<textarea name=\"" + this.name + "\" id=\"" + this.id + "\">");
        if (this.value != null) {
            out.write(this.value);
        }
        out.write("</textarea>");
        if (this.validationMessage != null) {

            out.write("<span class=\"invalid\">");
            out.write(this.validationMessage);
            out.write("</span>");

        }
    }

}
