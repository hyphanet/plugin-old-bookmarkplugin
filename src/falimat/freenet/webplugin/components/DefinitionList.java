package falimat.freenet.webplugin.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class DefinitionList extends AbstractHtmlComponent {

    private Map<String, String> nameValueMap = new TreeMap<String, String>();
    
    private List<String> keyOrderList = new LinkedList<String>();

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        if (this.nameValueMap.isEmpty()) {
            return;
        }
        out.write("<dl>");
        for (String key : this.keyOrderList) {
            out.write("<dt>" + key + "</dt>");
            out.write("<dd>" + this.nameValueMap.get(key) + "</dd>");
        }
        out.write("</dl>");
    }

    public void addEntry(String title, String description) {
        this.keyOrderList.add(title);
        this.nameValueMap.put(title, description);
    }

    public void removeEntry(String title) {
        this.keyOrderList.remove(title);
        this.nameValueMap.remove(title);
    }

}
