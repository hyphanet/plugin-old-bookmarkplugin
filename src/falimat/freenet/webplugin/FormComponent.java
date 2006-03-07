package falimat.freenet.webplugin;

import freenet.pluginmanager.HTTPRequest;

public interface FormComponent {
    void setValueFromRequest(HTTPRequest request);

    boolean validate(HTTPRequest request);

    void reset();
}
