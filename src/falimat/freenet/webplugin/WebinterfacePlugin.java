package falimat.freenet.webplugin;

import java.io.StringWriter;
import java.net.URISyntaxException;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.HTTPRequest;
import freenet.pluginmanager.PluginRespirator;

public abstract class WebinterfacePlugin implements FredPlugin, FredPluginHTTP {

    private HtmlPage rootPage;

    public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {

        String path = request.getPath();

        if (path.endsWith("css")) {
            try {
                throw new PluginHTTPException(200, "text/css", "CSS", StylesheetLoader.getStylesheet(path));
            } catch (PageNotFoundException e) {
                throw new PluginHTTPException(404, "text/plain", "NOTFOUND", e.toString());
            }
        }

        // TODO: put this in HTTPRequest ?

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        HtmlPage pageToBeRendered;
        if (path.length() == 0) {
            pageToBeRendered = this.rootPage;
        } else {
            try {
                pageToBeRendered = this.rootPage.getSubPage(path);
            } catch (PageNotFoundException e) {
                pageToBeRendered = new ErrorPage("404 - Page Not Found: " + request, e);
            }
        }

        if (request.isParameterSet(HtmlWriter.ACTION)) {
            String actionId = request.getParam(HtmlWriter.ACTION);
            Action action = pageToBeRendered.findAction(actionId);
            if (action == null) {
                throw new PluginHTTPException(400, "text/plain", "BADR", "Bad Request: no action found with id "
                        + actionId);
            }
            try {
                action.execute(request);
            } catch (RuntimeException e) {
                pageToBeRendered = new ErrorPage("501 - Unexpected Excepton: ", e);
            }
        }

        return renderPage(pageToBeRendered);
    }

    private String renderPage(HtmlPage pageToBeRendered) {
        StringWriter stringWriter = new StringWriter();
        HtmlWriter htmlWriter = new HtmlWriter(pageToBeRendered, stringWriter);
        pageToBeRendered.renderHtml(htmlWriter);
        return stringWriter.toString();
    }

    public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
        return this.renderPage(new ErrorPage("HTTP PUT is not supported by this plugin.", null));
    }

    public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
        return this.renderPage(new ErrorPage("HTTP POST is not supported by this plugin.", null));
    }

    public abstract void runPlugin(PluginRespirator pr);

    public abstract void terminate();

    protected HtmlPage getRootPage() {
        if (this.rootPage == null) {
            this.rootPage = new HtmlPage("Freenet Plugin");
        }
        return this.rootPage;
    }

    public void setRootPage(HtmlPage page) {
        this.rootPage = page;
    }
}
