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
 * Handles server-related placeholders with multi-locale date formatting.
 *
 * <p>This handler provides functionality for:
 * <ul>
 *   <li>Date formatting with 30+ supported locales</li>
 *   <li>Customizable date patterns</li>
 *   <li>Fallback to default locale for invalid inputs</li>
 * </ul>
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class ServerPlaceholderHandler implements PlaceholderHandler {

    private static final String DATE_COMMAND = "date";
    private static final Map<String, Locale> SUPPORTED_LOCALES = initializeSupportedLocales();

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        if (args.size() > 1 && DATE_COMMAND.equalsIgnoreCase(args.get(1))) {
            return handleDatePlaceholder(args);
        }
        return null;
    }

    /**
     * Handles date placeholder requests with optional locale specification.
     *
     * @param args the placeholder arguments
     * @return formatted date string
     */
    private String handleDatePlaceholder(List<String> args) {
        if (args.size() < 3) {
            return formatCurrentDate(getDefaultLocale());
        }

        Locale locale = parseLocale(args.get(2))
                .orElse(getDefaultLocale());

        return formatCurrentDate(locale);
    }

    /**
     * Parses a locale string to a Locale object.
     *
     * @param localeInput the locale identifier
     * @return Optional containing the Locale if valid
     */
    private Optional<Locale> parseLocale(String localeInput) {
        String normalized = localeInput.toLowerCase().trim();

        if (!SUPPORTED_LOCALES.containsKey(normalized)) {
            return Optional.empty();
        }

        return Optional.of(SUPPORTED_LOCALES.get(normalized));
    }

    /**
     * Formats the current date using the specified locale.
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
     * Gets the default locale from configuration.
     *
     * @return configured default Locale
     */
    private Locale getDefaultLocale() {
        String defaultLocale = Configuration.DATE.DEFAULT_LOCALE.toLowerCase();
        return SUPPORTED_LOCALES.getOrDefault(defaultLocale, Locale.of("en", "US"));
    }

    /**
     * Initializes the map of supported locales.
     *
     * @return immutable map of locale identifiers to Locale objects
     */
    private static Map<String, Locale> initializeSupportedLocales() {
        return Map.ofEntries(
                // Turkish
                Map.entry("tr", Locale.of("tr", "TR")),
                Map.entry("tr-tr", Locale.of("tr", "TR")),

                // English variants
                Map.entry("en", Locale.of("en", "US")),
                Map.entry("en-us", Locale.of("en", "US")),
                Map.entry("en-gb", Locale.of("en", "GB")),

                // European languages
                Map.entry("de", Locale.of("de", "DE")),
                Map.entry("de-de", Locale.of("de", "DE")),
                Map.entry("fr", Locale.of("fr", "FR")),
                Map.entry("fr-fr", Locale.of("fr", "FR")),
                Map.entry("es", Locale.of("es", "ES")),
                Map.entry("es-es", Locale.of("es", "ES")),
                Map.entry("it", Locale.of("it", "IT")),
                Map.entry("it-it", Locale.of("it", "IT")),
                Map.entry("pt", Locale.of("pt", "PT")),
                Map.entry("pt-pt", Locale.of("pt", "PT")),
                Map.entry("pt-br", Locale.of("pt", "BR")),
                Map.entry("nl", Locale.of("nl", "NL")),
                Map.entry("nl-nl", Locale.of("nl", "NL")),
                Map.entry("pl", Locale.of("pl", "PL")),
                Map.entry("pl-pl", Locale.of("pl", "PL")),

                // Nordic languages
                Map.entry("sv", Locale.of("sv", "SE")),
                Map.entry("sv-se", Locale.of("sv", "SE")),
                Map.entry("no", Locale.of("no", "NO")),
                Map.entry("no-no", Locale.of("no", "NO")),
                Map.entry("da", Locale.of("da", "DK")),
                Map.entry("da-dk", Locale.of("da", "DK")),
                Map.entry("fi", Locale.of("fi", "FI")),
                Map.entry("fi-fi", Locale.of("fi", "FI")),

                // Slavic languages
                Map.entry("ru", Locale.of("ru", "RU")),
                Map.entry("ru-ru", Locale.of("ru", "RU")),

                // Asian languages
                Map.entry("ja", Locale.of("ja", "JP")),
                Map.entry("ja-jp", Locale.of("ja", "JP")),
                Map.entry("zh", Locale.of("zh", "CN")),
                Map.entry("zh-cn", Locale.of("zh", "CN")),
                Map.entry("zh-tw", Locale.of("zh", "TW")),
                Map.entry("ko", Locale.of("ko", "KR")),
                Map.entry("ko-kr", Locale.of("ko", "KR")),

                // Middle Eastern languages
                Map.entry("ar", Locale.of("ar", "SA")),
                Map.entry("ar-sa", Locale.of("ar", "SA"))
        );
    }
}