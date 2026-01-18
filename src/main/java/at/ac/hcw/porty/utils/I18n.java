package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.Language;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class I18n {
    private static final ObjectProperty<Language> language =
            new SimpleObjectProperty<>(Language.fromSystemDefault());

    private I18n() {}

    public static Language getLanguage() { return language.get(); }
    public static void setLanguage(Language lang) {
        if (lang == null || lang == getLanguage()) return;
        language.set(lang);
        // Clear default RB caches to be safe across platforms
        ResourceBundle.clearCache();
        ResourceBundle.clearCache(Thread.currentThread().getContextClassLoader());
        // Keep default locale in sync for MessageFormat and other fallbacks
        Locale.setDefault(lang.toTextLocale());
    }
    public static ObjectProperty<Language> languageProperty() { return language; }

    /**
     * Module-safe, UTF-8 loader with simple fallback:
     * 1) messages_<lang>.properties
     * 2) messages.properties (base)
     */
    public static ResourceBundle getBundle() {
        Locale locale = getLanguage().toTextLocale();
        String basePath = "/at/ac/hcw/porty/i18n/messages";
        String langSuffix = "_" + locale.getLanguage();

        ResourceBundle rb = tryLoad(basePath + langSuffix + ".properties");
        if (rb == null) rb = tryLoad(basePath + ".properties");
        if (rb == null) {
            // As a last resort, return an empty bundle to avoid NPE
            try { rb = new PropertyResourceBundle(new java.io.StringReader("")); } catch (Exception ignored) {}
        }
        return rb;
    }

    private static ResourceBundle tryLoad(String path) {
        try (InputStream is = I18n.class.getResourceAsStream(path)) {
            if (is == null) return null;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static StringBinding bind(String key, Object... args) {
        return Bindings.createStringBinding(() -> {
            ResourceBundle rb = getBundle();
            String pattern = rb.containsKey(key) ? rb.getString(key) : "§" + key + "§";
            MessageFormat mf = new MessageFormat(pattern, getLanguage().toTextLocale());
            return mf.format(args == null ? new Object[0] : args);
        }, language);
    }
}