package falimat.freenet.webplugin;

import freenet.pluginmanager.PluginHTTPRequest;


public interface Action {
    String getId();
    void execute(PluginHTTPRequest request);
}
