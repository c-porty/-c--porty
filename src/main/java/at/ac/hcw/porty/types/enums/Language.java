package at.ac.hcw.porty.types.enums;

import java.util.Locale;

public enum Language {
    DE, EN;

    public Locale toTextLocale() {
        return switch (this) {
            case DE -> Locale.GERMAN;
            case EN -> Locale.ENGLISH;
        };
    }

    public static Language fromSystemDefault() {
        String lang = Locale.getDefault().getLanguage();
        return "de".equalsIgnoreCase(lang) ? DE : EN;
    }
}
