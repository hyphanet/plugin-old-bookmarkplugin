package falimat.freenet.webplugin.components;

import java.util.LinkedList;
import java.util.List;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.Action;
import falimat.freenet.webplugin.ActionComponent;
import falimat.freenet.webplugin.FormComponent;
import falimat.freenet.webplugin.HtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import freenet.pluginmanager.HTTPRequest;

public class Form extends AbstractHtmlComponent implements ActionComponent {

    private Action action;

    private List<HtmlComponent> components = new LinkedList<HtmlComponent>();

    private boolean hidden = false;
    
    public void addComponent(HtmlComponent component) {
        this.components.add(component);
    }

    public void show() {
        this.hidden = false;
    }
    
    public void hide() {
        this.hidden = true;
    }
    
    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        
        if (this.hidden) {
            return;
        }
        out.beginForm(contextPage);

        for (HtmlComponent component : this.components) {
            component.renderHtml(out, contextPage);
        }

        out.endForm();
    }

    public Action getAction(String id) {

        if (this.action != null && this.action.getId().equals(id)) {
            return this.action;
        }

        for (HtmlComponent component : this.components) {
            if (component instanceof ActionComponent) {
                Action action = ((ActionComponent) component).getAction(id);
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean validate(HTTPRequest request) {
        for (HtmlComponent component : this.components) {
            if (component instanceof FormComponent) {
                ((FormComponent) component).setValueFromRequest(request);
            }
        }
        boolean overallSuccess = true;
        for (HtmlComponent component : this.components) {
            if (component instanceof FormComponent) {
                boolean success = ((FormComponent) component).validate(request);
                if (!success) {
                    overallSuccess = false;
                }
            }
        }
        return overallSuccess;
    }

    public void resetFormValues() {
        for (HtmlComponent component : this.components) {
            if (component instanceof FormComponent) {
                ((FormComponent) component).reset();
            }
        }
    }

    public void removeAllComponents() {
      this.components.clear();
    }

}
