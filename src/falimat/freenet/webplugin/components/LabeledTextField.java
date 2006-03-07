package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.FormComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class LabeledTextField extends AbstractFormComponent implements FormComponent {

    private static int textFieldCount = 0;

    private String id = "txtfld_" + textFieldCount++;

    public LabeledTextField(String name, String label) {
        this.name = name;
        this.label = label;
    }

    @Override
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        out.writeLabel(this.id, this.label);

        out.writeInput("text", this.name, this.id, this.value);
        if (this.validationMessage != null) {
            out.write("<span class=\"invalid\">");
            out.write(this.validationMessage);
            out.write("</span>");
        }
    }

}
