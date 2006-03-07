package falimat.freenet.webplugin;

import xomat.util.ParamException;

public class PageNotFoundException extends ParamException {

    private static final long serialVersionUID = -8403407986983450467L;

    public PageNotFoundException(HtmlPage page, String path) {
        super("Page {0} has no subpage with path ''{1}''", page.getName(), path);
    }

    public PageNotFoundException(String msg, String param, Exception e) {
        super(msg, param, e);
    }
}
