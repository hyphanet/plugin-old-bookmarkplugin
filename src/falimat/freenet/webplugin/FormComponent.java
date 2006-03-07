package falimat.freenet.webplugin;

import freenet.pluginmanager.PluginHTTPRequest;

public interface FormComponent {
    void setValueFromRequest(PluginHTTPRequest request);

    boolean validate(PluginHTTPRequest request);

    void reset();
}
