package miayeelight.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static miayeelight.Main.log;

public class Strings {

    private static ResourceBundle resourceBundle;

    private Strings() {
    }

    public static void configMessages(String lang) {
        String pacchettoLingua = switch (lang) {
            case "it" -> ".ita";
            case "pt" -> ".ptg";
            case "es" -> ".esp";
            case "fr" -> ".fra";
            case "bs" -> ".bre";
            default -> ".eng";
        };
        try {
            resourceBundle = ResourceBundle.getBundle("miayeelight.lang" + pacchettoLingua);
        } catch (MissingResourceException e) {
            log(e);
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

    public static String get(final Class<?> classe, String key) {
        return get("%s.%s".formatted(classe.getSimpleName(), key));
    }

}