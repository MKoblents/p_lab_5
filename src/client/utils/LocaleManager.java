package client.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {
    private static Locale currentLocale = new Locale("ru", "RU");
    private static ResourceBundle bundle;

    static {
        reloadBundle();
    }

    private static void reloadBundle() {
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        reloadBundle();
    }

    public static void setLocale(String language, String country) {
        setLocale(new Locale(language, country));
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
    public static String get(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return java.text.MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static String getAppTitle() {
        return get("app.title");
    }

    public static String getDeveloper() {
        return get("app.developer");
    }
}