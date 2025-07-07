package internationalization;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author tiya
 */
public class configLanguange {

    private static configLanguange instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private configLanguange() {
        setLanguange("Inggris");
    }

    public static configLanguange getInstance() {
        if (instance == null) {
            instance = new configLanguange();
        }
        return instance;
    }

    public void setLanguange(String language) {
        switch (language.toLowerCase()) {
            case "indonesia":
                currentLocale = new Locale("id", "ID");
                break;
            case "inggris":
                currentLocale = new Locale("en", "US");
                break;
            default:
                currentLocale = Locale.getDefault();
        }
        bundle = ResourceBundle.getBundle("internationalization.messages", currentLocale);
    }

    public String getLanguangeName() {
        if (currentLocale.equals(new Locale("id", "ID"))) {
            return "Indonesia";
        } else if (currentLocale.equals(new Locale("en", "US"))) {
            return "Inggris";
        } else {
            return "Inggris"; // default fallback
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public Locale getLocale() {
        return currentLocale;
    }
}
