package falimat.freenet.webplugin;

import freenet.pluginmanager.HTTPRequest;


public interface Action {
    String getId();
    void execute(HTTPRequest request);
}
