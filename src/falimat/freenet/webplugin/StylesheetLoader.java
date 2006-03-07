package falimat.freenet.webplugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public  class StylesheetLoader {
 
    private final static Log log = LogFactory.getLog(StylesheetLoader.class);
    
    private final static Map<String, String> cachedStylesheets = new HashMap<String, String>();
    
    public static synchronized String getStylesheet(String url) throws PageNotFoundException {
        try {
            url = "/"+url;
            if (!cachedStylesheets.containsKey(url)) {
                InputStream inputStream = StylesheetLoader.class.getResourceAsStream(url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringWriter stringWriter = new StringWriter();
                while(true) {
                    String nextLine = reader.readLine();
                    if (nextLine==null) {
                        break;
                    }
                    stringWriter.write(nextLine+"\n");
                }
                inputStream.close();
                // cachedStylesheets.put(url, stringWriter.toString());
                return stringWriter.toString();
            }
            return cachedStylesheets.get(url);
        } catch (Exception e) {
            String msg = "Couldn't find stylesheet at {0}";
            log.warn(MessageFormat.format(msg, url), e);
            throw new PageNotFoundException(msg, url, e);
        }
    }
}
