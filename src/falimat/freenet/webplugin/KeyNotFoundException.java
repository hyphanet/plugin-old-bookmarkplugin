/**
 * 
 */
package falimat.freenet.webplugin;

import xomat.util.ParamException;

public class KeyNotFoundException extends ParamException {
    public KeyNotFoundException(String uri) {
        super("Failed to fetch freenet key  {0}", uri);
    }
}