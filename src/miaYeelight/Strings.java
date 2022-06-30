package miaYeelight;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class Strings {
	private static ResourceBundle RESOURCE_BUNDLE;

	public static void configMessages() {
		String langpack;
		switch (Locale.getDefault().getLanguage()) {
			case "it" : langpack = ".ita"; break;
			case "pt" : langpack = ".ptg"; break;
			case "es" : langpack = ".esp"; break;
			case "en" : langpack = ".eng"; break;
			default : langpack = ".eng";
		}
		try {RESOURCE_BUNDLE = ResourceBundle.getBundle("miaYeelight.Lang" + langpack);}
		catch (MissingResourceException e) {
			e.printStackTrace(); 
			RESOURCE_BUNDLE = null;
		}
	}

	public static String get(String key) {
		try {return RESOURCE_BUNDLE.getString(key);} 
		catch (Exception e) {return '!' + key + '!';}
	}
}
