package falimat.freenet.webplugin;

import falimat.freenet.webplugin.components.MessageArea;

public class ErrorPage extends HtmlPage {
    public ErrorPage(String msg, Throwable t) {
        super("Error Page"); // TODO: This is weird
        super.setTitle("Error in freenet plugin: " + msg);
        MessageArea messageArea = new MessageArea();
        messageArea.setHeadline(msg);
        messageArea.setMessage("<pre>" + HtmlPage.getStacktrace(t) + "</pre>");
        super.addComponent(messageArea);
    } 
}
