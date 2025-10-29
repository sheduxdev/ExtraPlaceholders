package dev.shedux.extraplaceholders.handler;

import dev.shedux.extraplaceholders.config.Configuration;
import org.bukkit.OfflinePlayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Handles server-related placeholders with multi-locale date formatting
 * Supports 30+ locales with customizable date patterns
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public final class ServerPlaceholderHandler implements PlaceholderHandler {

    private static final String DATE_COMMAND = "date";
    private static final int MIN_DATE_ARGS = 2;
    private static final int LOCALE_ARG_INDEX = 2;

    private static final Map<String, Locale> SUPPORTED_LOCALES = initializeSupportedLocales();

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        if (!hasMinimumArgs(args, MIN_DATE_ARGS)) {
            return null;
        }

        if (DATE_COMMAND.equalsIgnoreCase(args.get(1))) {
            return handleDatePlaceholder(args);
        }

        return null;
    }

    /**
     * Handles date placeholder requests with optional locale specification
     *
     * @param args the placeholder arguments
     * @return formatted date string
     */
    private String handleDatePlaceholder(List<String> args) {
        Locale locale = getLocaleFromArgs(args)
                .orElse(getDefaultLocale());

        return formatCurrentDate(locale);
    }

    /**
     * Extracts locale from arguments if present
     *
     * @param args the placeholder arguments
     * @return Optional containing the Locale if valid
     */
    private Optional<Locale> getLocaleFromArgs(List<String> args) {
        if (args.size() <= LOCALE_ARG_INDEX) {
            return Optional.empty();
        }

        return parseLocale(args.get(LOCALE_ARG_INDEX));
    }

    /**
     * Parses a locale string to a Locale object
     *
     * @param localeInput the locale identifier
     * @return Optional containing the Locale if valid
     */
    private Optional<Locale> parseLocale(String localeInput) {
        if (localeInput == null || localeInput.isEmpty()) {
            return Optional.empty();
        }

        String normalized = localeInput.toLowerCase().trim();
        return Optional.ofNullable(SUPPORTED_LOCALES.get(normalized));
    }

    /**
     * Formats the current date using the specified locale
     *
     * @param locale the locale to use for formatting
     * @return formatted date string
     */
    private String formatCurrentDate(Locale locale) {
        try {
            String pattern = Configuration.DATE.DATE_PATTERN;
            SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
            return formatter.format(new Date());
        } catch (Exception e) {
            return Configuration.MESSAGES.INVALID_LOCALE;
        }
    }

    /**
     * Gets the default locale from configuration
     *
     * @return configured default Locale
     */
    private Locale getDefaultLocale() {
        String defaultLocale = Configuration.DATE.DEFAULT_LOCALE.toLowerCase();
        return SUPPORTED_LOCALES.getOrDefault(defaultLocale, Locale.US);
    }

    /**
     * Initializes the map of supported locales
     * Uses immutable map for thread safety and performance
     *
     * @return immutable map of locale identifiers to Locale objects
     */
    private static Map<String, Locale> initializeSupportedLocales() {
        return Map.ofEntries(
                // Turkish
                Map.entry("tr", new Locale("tr", "TR")),
                Map.entry("tr-tr", new Locale("tr", "TR")),

                // English variants
                Map.entry("en", Locale.US),
                Map.entry("en-us", Locale.US),
                Map.entry("en-gb", Locale.UK),

                // European languages
                Map.entry("de", Locale.GERMANY),
                Map.entry("de-de", Locale.GERMANY),
                Map.entry("fr", Locale.FRANCE),
                Map.entry("fr-fr", Locale.FRANCE),
                Map.entry("es", new Locale("es", "ES")),
                Map.entry("es-es", new Locale("es", "ES")),
                Map.entry("it", Locale.ITALY),
                Map.entry("it-it", Locale.ITALY),
                Map.entry("pt", new Locale("pt", "PT")),
                Map.entry("pt-pt", new Locale("pt", "PT")),
                Map.entry("pt-br", new Locale("pt", "BR")),
                Map.entry("nl", new Locale("nl", "NL")),
                Map.entry("nl-nl", new Locale("nl", "NL")),
                Map.entry("pl", new Locale("pl", "PL")),
                Map.entry("pl-pl", new Locale("pl", "PL")),

                // Nordic languages
                Map.entry("sv", new Locale("sv", "SE")),
                Map.entry("sv-se", new Locale("sv", "SE")),
                Map.entry("no", new Locale("no", "NO")),
                Map.entry("no-no", new Locale("no", "NO")),
                Map.entry("da", new Locale("da", "DK")),
                Map.entry("da-dk", new Locale("da", "DK")),
                Map.entry("fi", new Locale("fi", "FI")),
                Map.entry("fi-fi", new Locale("fi", "FI")),

                // Slavic languages
                Map.entry("ru", new Locale("ru", "RU")),
                Map.entry("ru-ru", new Locale("ru", "RU")),

                // Asian languages
                Map.entry("ja", Locale.JAPAN),
                Map.entry("ja-jp", Locale.JAPAN),
                Map.entry("zh", Locale.CHINA),
                Map.entry("zh-cn", Locale.CHINA),
                Map.entry("zh-tw", Locale.TAIWAN),
                Map.entry("ko", Locale.KOREA),
                Map.entry("ko-kr", Locale.KOREA),

                // Middle Eastern languages
                Map.entry("ar", new Locale("ar", "SA")),
                Map.entry("ar-sa", new Locale("ar", "SA"))
        );
    }
}