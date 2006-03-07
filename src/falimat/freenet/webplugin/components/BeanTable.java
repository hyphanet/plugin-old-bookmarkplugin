package falimat.freenet.webplugin.components;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class BeanTable extends AbstractHtmlComponent {

    public static final CellRenderer PUBLIC_SSK_REND = new CellRenderer() {

        public void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage) {
            String ssk = (String) value;
            String croppedSSK = ssk.substring(0, 15) + "..."+ssk.substring(ssk.length()-15);
            out.writeTooltipItem(ssk, croppedSSK, "ssk");
        }

    };

    public static final CellRenderer FREENET_LINK_RENDERER = new CellRenderer() {

        public void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage) {
            String ssk = (String) value;
            String croppedSSK = ssk.substring(0, 15) + "..."+ssk.substring(ssk.length()-15);
            out.writeLink("/"+ssk, croppedSSK, "View the content of this slot as xml" );
        }

    };
    
    public static final CellRenderer DEFAULT_RENDERER = new CellRenderer() {

        public void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage) {
            out.write(value.toString());
        }

    };

    public static final CellRenderer DATE_TIME_RENDERER = new CellRenderer() {

        private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        public void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage) {
            Long millis = (Long)value;
            if (millis<=0) {
                out.write("-");
                return;
            }
            Date date = new Date(millis);
            synchronized (dateFormat) {
                out.write(dateFormat.format(date));
            }

        }

    };

    public static final CellRenderer DURATION_RENDERER = new CellRenderer() {

        public void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage) {
            Integer millis = (Integer) value;
            int minutes = millis.intValue() / 1000 / 60;
            int hours = minutes / 60;
            minutes = minutes - hours * 60;
            if (hours > 0) {
                out.write(hours + " h ");
            }
            if (minutes > 0) {
                out.write(minutes + " min");
            }

        }

    };

    private List<String> propertyNameList = new LinkedList<String>();

    private Map<String, CellRenderer> propertyCellRendererMap = new HashMap<String, CellRenderer>();

    private List<Object> beanList = new LinkedList<Object>();

    private BeanTableDataSource dataProvider;

    public void addRow(Object bean) {
        this.beanList.add(bean);
    }

    public void addColumn(String string) {
        this.propertyNameList.add(string);
    }

    public void addColumn(String propertyName, CellRenderer cellRenderer) {
        this.addColumn(propertyName);
        this.propertyCellRendererMap.put(propertyName, cellRenderer);
    }

    interface CellRenderer {
        void renderCellContent(Object value, HtmlWriter out, HtmlPage contextPage);
    }

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {

        if (this.dataProvider != null) {
            synchronized (this.beanList) {
                this.beanList.clear();
                Object[] newValues = this.dataProvider.getRows();
                for (Object o : newValues) {
                    this.beanList.add(o);
                }
            }
        }

        out.append("<table><thead>");
        for (Iterator propertyNameIterator = this.propertyNameList.iterator(); propertyNameIterator.hasNext();) {
            String propertyName = (String) propertyNameIterator.next();
            out.append("<th>" + this.getColumnHeader(propertyName) + "</th>");
        }
        out.append("</tr></thead><tbody>");

        for (Iterator objectIterator = this.beanList.iterator(); objectIterator.hasNext();) {
            out.append("<tr>");
            Object nextBean = (Object) objectIterator.next();

            try {
                Map getMethodMap = this.findAvailableGetters(nextBean);

                for (Iterator propertyNameIterator = this.propertyNameList.iterator(); propertyNameIterator.hasNext();) {
                    String propertyName = (String) propertyNameIterator.next();
                    out.append("<td>");
                    try {
                        Method getter = (Method) getMethodMap.get(propertyName);
                        if (getter == null) {
                            String msg = "Bean of " + nextBean + " does not have a property named " + propertyName;
                            throw new RuntimeException(msg);
                        }
                        Object value = getter.invoke(nextBean, (Object[])new String[0]);

                        this.formatValue(propertyName, value, out, contextPage);

                    } catch (Exception e) {
                        out.append("<span class=\"error\">n/a</span>");
                        out.writeStacktrace(e, true);
                    }
                    out.append("</td>");
                }
            } catch (Exception e) {
                out.append("<td colspan=\"" + propertyNameList.size() + "\">");
                out.append("<span class=\"error\">n/a</span>");
                out.writeStacktrace(e, true);

                out.append("</td>");
            }
            out.append("</tr>");
        }

        out.append("</tbody></table>");
    }

    private Map<String, Method> findAvailableGetters(Object bean) throws IntrospectionException {
        // find all properties with introspection
        BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

        // gather all getters in a map with the property name as key
        Map<String, Method> getMethodMap = new HashMap<String, Method>();
        for (int i = 0; i < properties.length; i++) {
            PropertyDescriptor descriptor = properties[i];
            Method getter = descriptor.getReadMethod();
            if (getter != null) {
                getMethodMap.put(descriptor.getName(), getter);
            }
        }
        return getMethodMap;
    }

    private void formatValue(String propertyName, Object value, HtmlWriter out, HtmlPage contextPage) {
        // TODO implement custom formatting for columns
        if (value == null) {
            out.write("n/a");
            return;
        }
        try {
            CellRenderer renderer = this.propertyCellRendererMap.get(propertyName);
            if (renderer == null) {
                renderer = DEFAULT_RENDERER;
            }
            renderer.renderCellContent(value, out, contextPage);
        } catch (Exception e) {
            out.write("n/a");
            out.writeStacktrace(e, true);
        }
    }

    public String getColumnHeader(String propertyName) {
        // TODO: maybe look up i18nized column header?
        return propertyName;
    }

    protected BeanTableDataSource getDataProvider() {
        return this.dataProvider;
    }

    public void setDataProvider(BeanTableDataSource dataProvider) {
        this.dataProvider = dataProvider;
    }

}
