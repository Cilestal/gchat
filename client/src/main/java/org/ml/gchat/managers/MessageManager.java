package org.ml.gchat.managers;

import org.ml.gchat.util.UTF8Control;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Date: 22.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class MessageManager {
    private static final String CLIENT_BUNDLE = "client_messages";

    private static ResourceBundle resourceBundle;

    static {
        loadBundle(Locale.getDefault());
    }

    public static String getMessage(String key){
        return resourceBundle.getString(key);
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static void loadBundle(Locale locale){
        UTF8Control utf8Control = new UTF8Control();
        resourceBundle = ResourceBundle.getBundle(CLIENT_BUNDLE, locale, utf8Control);
    }
}
