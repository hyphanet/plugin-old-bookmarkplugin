package falimat.freenet.webplugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HtmlPage {

    private final static String doctype = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";

    private String title = "Untitled Freenet Plugin";

    private final List<HtmlComponent> componentList = new ArrayList<HtmlComponent>();

    private final Map<String, HtmlPage> subPagesMap = new HashMap<String, HtmlPage>();

    private String path = "/";

    private String name;

    private int level = 0;

    public HtmlPage(String name) {
        this.name = name;
        this.title = name;
    }

    public void constructComponents() {

    }

    public void renderHtml(HtmlWriter out) {
        out.append(doctype + "\n");
        out.append("<html>");
        this.renderHeadHtml(out);
        this.renderBodyHtml(out);
        out.append("</html>");
    }

    public Action findAction(String actionId) {
        for (Iterator iter = this.componentList.iterator(); iter.hasNext();) {
            HtmlComponent component = (HtmlComponent) iter.next();
            if (component instanceof ActionComponent) {
                Action action = ((ActionComponent) component).getAction(actionId);
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }

    public HtmlPage createSubPage(String name, String pathElement) {
        if (pathElement.contains("/")) {
            throw new IllegalArgumentException("the id of the subpage must not contain the /");
        }
        HtmlPage subpage = new HtmlPage(name);
        this.addSubpage(subpage, pathElement);
        return subpage;
    }

    public void addSubpage(HtmlPage page, String pathElement) {
        page.setTitle(this.title + " - " + page.getName());
        page.level = this.level + 1;
        page.path = this.getPath() + pathElement + "/";
        synchronized (this.subPagesMap) {
            this.subPagesMap.put(pathElement, page);
        }
    }

    public HtmlPage[] listSubpages() {
        synchronized (this.subPagesMap) {
            HtmlPage[] subpages = new HtmlPage[this.subPagesMap.size()];
            this.subPagesMap.values().toArray(subpages);
            return subpages;
        }
    }

    private void renderHeadHtml(HtmlWriter out) {
        out.append("<head>");
        out.append("<title>" + this.title + "</title>");
        out.writeCssLink("/falimat/freenet/webplugin/style.css");
        out.append("</head>");
    }

    public void addComponent(HtmlComponent component) {
        synchronized (this.componentList) {
            this.componentList.add(component);
        }
    }

    public void removeCompoment(HtmlComponent component) {
        synchronized (this.componentList) {
            this.componentList.remove(component);
        }
    }

    public void addToAll(HtmlComponent component) {
        this.addComponent(component);
        HtmlPage[] subpages = this.listSubpages();
        for (int i = 0; i < subpages.length; i++) {
            subpages[i].addToAll(component);
        }
    }

    private void renderBodyHtml(HtmlWriter out) {
        out.append("<body>");

        out.append("<h1>"+this.name+"</h1>");
        
        synchronized (this.componentList) {
            for (int i = 0; i < componentList.size(); i++) {
                HtmlComponent component = (HtmlComponent) componentList.get(i);
                try {
                    component.renderHtml(out, this);
                } catch (Exception e) {
                    e.printStackTrace();

                    out.beginDiv("error");

                    out.append("Failed to render HtmlComponent " + component.toString() + ": ");
                    out.append("<pre>");
                    out.writeStacktrace(e, false);
                    out.append("</pre>)");

                    out.endDiv();
                }
            }
        }

        out.append("</body>");
    }

    public static String getStacktrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HtmlPage getSubPage(String path) throws PageNotFoundException {
        if (path == null || path.length() == 0) {
            throw new PageNotFoundException(this, path);
        }
        synchronized (this.subPagesMap) {
            int slashPosition = path.indexOf('/');
            if (slashPosition > 0) {
                String firstPathSegment = path.substring(0, slashPosition);
                String restOfPath = path.substring(slashPosition + 1);
                HtmlPage page = (HtmlPage) this.subPagesMap.get(firstPathSegment);
                if (page == null) {
                    throw new PageNotFoundException(this, path);
                }
                return page.getSubPage(restOfPath);
            } else {
                HtmlPage page = (HtmlPage) this.subPagesMap.get(path);
                if (page == null) {
                    throw new PageNotFoundException(this, path);
                }
                return page;
            }
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public int getLevel() {
        return this.level;
    }

}
