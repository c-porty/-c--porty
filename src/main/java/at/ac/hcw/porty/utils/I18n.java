package at.ac.hcw.porty.utils;

import at.ac.hcw.porty.types.enums.Language;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class I18n {
    private static final ObjectProperty<Language> language = new SimpleObjectProperty<>(Language.fromSystemDefault());

    public static Language getLanguage() { return language.get(); }
    public static void setLanguage(Language lang) { language.set(lang); }
    public static ObjectProperty<Language> languageProperty() { return language; }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("at.ac.hcw.porty.i18n.messages", getLanguage().toTextLocale());
    }

    public static StringBinding bind(String key, Object... args) {
        return Bindings.createStringBinding(() -> {
            ResourceBundle rb = getBundle();
            String pattern = rb.containsKey(key) ? rb.getString(key) : "§" + key + "§";
            MessageFormat mf = new MessageFormat(pattern, getLanguage().toTextLocale());
            return mf.format(args);
        }, language);
    }
}