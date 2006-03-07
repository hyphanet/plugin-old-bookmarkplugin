package falimat.freenet.webplugin.components;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import freenet.pluginmanager.HTTPRequest;

public abstract class AbstractFormComponent extends AbstractHtmlComponent {

    protected String name;

    private String defaultValue;

    protected String value = null;

    private Map<String, Pattern[]> validationPatterns = new HashMap<String, Pattern[]>();

    private boolean required = false;

    protected String validationMessage;

    protected String label;

    public abstract void renderHtml(HtmlWriter out, HtmlPage contextPage);

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValueFromRequest(HTTPRequest request) {
        this.value = request.getParam(this.name).trim();
    }

    public boolean validate(HTTPRequest request) {
        String newValue = this.value;
        if (newValue.length() == 0) {
            if (this.required) {
                this.validationMessage = "^ You have to enter a value here.";
                return false;
            } else {
                this.validationMessage = null;
                return true;
            }
        }
        for (String msg : this.validationPatterns.keySet()) {
            Pattern[] patterns = this.validationPatterns.get(msg);
            boolean foundMatch = false;
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(newValue);
                if (matcher.matches()) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                this.validationMessage = "^ "+msg;
                return false;
            }
        }
        this.validationMessage = null;
        return true;

    }

    public void reset() {
        this.value = this.defaultValue;
        this.validationMessage = null;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setValidation(String message, Pattern... patterns) {
        this.validationPatterns.put(message, patterns);
    }

    public void showValidationMessage(String string) {
        this.validationMessage = string;
    }

}
