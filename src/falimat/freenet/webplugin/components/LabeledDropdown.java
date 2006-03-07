package falimat.freenet.webplugin.components;

import falimat.freenet.webplugin.FormComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import freenet.pluginmanager.HTTPRequest;

public class LabeledDropdown extends AbstractFormComponent implements FormComponent {

    private static int textFieldCount = 0;

    private String id = "txtfld_" + textFieldCount++;

    private String[] possibleValues;

    private boolean allowEmptySelection;

    public LabeledDropdown(String name, String label, boolean allowEmptySelection, String... possibleValues) {
        this.name = name;
        this.label = label;

        this.allowEmptySelection = allowEmptySelection;
        this.possibleValues = possibleValues;
    }

    @Override
    public void setValueFromRequest(HTTPRequest request) {
        try {
            int index = request.getIntParam(this.name, -1);
            if (index >= 0) {
                this.value = this.possibleValues[index];
            } else {
                this.value = "";
            }
        } catch (Exception e) {
            this.value = "";
        }
    }

    @Override
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        out.writeLabel(this.id, this.label);

        out.write("<select name=\"" + this.name + "\" id=\"" + this.id + "\">");
        if (this.allowEmptySelection) {
            out.write("<option");

            if (this.value == null || this.value.length() == 0) {
                out.write(" selected=\"selected\"");
            }
            out.write("></option>");
        }
        for (int i = 0; i < possibleValues.length; i++) {
            out.write("<option value=\"" + i + "\"");
            if (possibleValues[i].equals(this.value)) {
                out.write(" selected=\"selected\"");
            }
            out.write(">");
            out.write(this.possibleValues[i]);
            out.write("</option>");
        }
        out.write("</select>");

        if (this.validationMessage != null) {

            out.write("<span class=\"invalid\">");
            out.write(this.validationMessage);
            out.write("</span>");

        }
    }

}
