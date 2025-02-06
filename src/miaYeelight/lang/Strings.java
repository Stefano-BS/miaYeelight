package miaYeelight.lang;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Strings {

    private static ResourceBundle resourceBundle;

    private Strings() {
    }

    public static void configMessages() {
        configMessages(Locale.getDefault().getLanguage());
    }

    public static void configMessages(String lang) {
        String pacchettoLingua = switch (lang) {
            case "it" -> ".ita";
            case "pt" -> ".ptg";
            case "es" -> ".esp";
            default -> ".eng";
        };
        try {
            resourceBundle = ResourceBundle.getBundle("miaYeelight.lang" + pacchettoLingua);
        } catch (MissingResourceException e) {
            resourceBundle = null;
        }
    }

    public static String get(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return '!' + key + '!';
        }
    }

}