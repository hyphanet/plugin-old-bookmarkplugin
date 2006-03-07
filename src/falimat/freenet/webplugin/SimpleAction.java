package falimat.freenet.webplugin;

import freenet.pluginmanager.PluginHTTPRequest;


public abstract class SimpleAction implements Action {

    private static long idCounter = 0;

    private String id = null;

    protected SimpleAction() {

    }

    protected SimpleAction(String id) {
        this.id = id;
    }

    public String getId() {
        if (this.id == null) {
            this.id = Long.toString(idCounter++);
        }
        return this.id;
    }

    public abstract void execute(PluginHTTPRequest request);

}
