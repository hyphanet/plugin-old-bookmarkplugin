package falimat.freenet.webplugin;

import falimat.freenet.webplugin.components.Form;
import freenet.pluginmanager.PluginHTTPRequest;

public abstract class FormAction extends SimpleAction {

    private Form form;
    
    private boolean validateBeforeExecution = true;

    public FormAction(Form form) {
        this.form = form;
    }

    public FormAction(Form form, boolean validateBeforeExecution) {
        this.form = form;
        this.validateBeforeExecution = validateBeforeExecution;   
    }

    @Override
    public void execute(PluginHTTPRequest request) {
        boolean validationSuccesfull = validateBeforeExecution ? this.form.validate(request) : true;
        if (validationSuccesfull) {
            this.onSubmit(request);
        }
    }
    
    protected abstract void onSubmit(PluginHTTPRequest request);
    
    protected abstract void onInvalidSubmission(PluginHTTPRequest request);

}
