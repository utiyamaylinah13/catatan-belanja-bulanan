package util;
import java.util.*;

public class Localutil {
    private static ResourceBundle bundle;

    public static void setLocale(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("resources.messages", locale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }
}
