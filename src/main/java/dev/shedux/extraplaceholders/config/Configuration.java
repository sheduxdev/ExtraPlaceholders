package dev.shedux.extraplaceholders.config;

import net.j4c0b3y.api.config.ConfigHandler;
import net.j4c0b3y.api.config.StaticConfig;

import java.io.File;

/**
 * Configuration manager for ExtraPlaceholders plugin
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class Configuration extends StaticConfig {

    public Configuration(File folder, ConfigHandler handler) {
        super(new File(folder, "config.yml"), handler);
    }

    /**
     * Message configuration section
     */
    public static class MESSAGES {
        @Comment("Reload Messages")
        public static String RELOAD_SUCCESS = "&aConfiguration successfully reloaded in &e<duration>ms&a!";
        public static String RELOAD_ERROR = "&cAn error occurred while reloading the configuration!";

        @Comment("Plugin Status Messages")
        public static String PLUGIN_ENABLED = "&aPlugin successfully enabled! &7v<version>";
        public static String PLUGIN_DISABLED = "&cPlugin disabled.";
        public static String EXPANSION_REGISTERED = "&aPlaceholderAPI integration completed successfully.";
        public static String EXPANSION_FAILED = "&cPlaceholderAPI registration failed!";

        @Comment("Dependency Messages")
        public static String PLACEHOLDER_API_NOT_FOUND = "&cPlaceholderAPI not found! Plugin will be disabled.";
        public static String BOLT_NOT_AVAILABLE = "&cBolt API is not available";
        public static String PHOENIX_NOT_AVAILABLE = "&cPhoenix API is not available";

        @Comment("Command Messages")
        public static String NO_PERMISSION = "&cYou don't have permission to use this command!";
        public static String PLUGIN_INFO_HEADER = "&8&m          &r &6ExtraPlaceholders &8&m          ";
        public static String PLUGIN_INFO_VERSION = "&eVersion: &f<version>";
        public static String PLUGIN_INFO_AUTHOR = "&eAuthor: &f<author>";
        public static String PLUGIN_INFO_BOLT = "&eBolt: <status>";
        public static String PLUGIN_INFO_PHOENIX = "&ePhoenix: <status>";
        public static String STATUS_ENABLED = "&aEnabled";
        public static String STATUS_DISABLED = "&cDisabled";

        @Comment("Kit Placeholder Messages")
        public static String KIT_OUT_OF_MATCH = "&7Out of match";
        public static String KIT_LOADING = "&eKit loading";
        public static String KIT_INVALID = "&cInvalid kit";
        public static String KIT_DEFAULT = "&7-";

        @Comment("Date Placeholder Messages")
        public static String INVALID_LOCALE = "&cInvalid locale format!";
    }

    /**
     * Date configuration section
     */
    public static class DATE {
        @Comment({
                "Date Format Settings",
                "Available locales: tr, en, de, fr, es, it, pt, ru, ja, zh, ar, ko, nl, pl, sv, no, da, fi",
                "Format pattern: d MMMM yyyy, EEE"})
        public static String DEFAULT_LOCALE = "tr-TR";
        public static String DATE_PATTERN = "d MMMM yyyy, EEE";
    }

    /**
     * Phoenix status configuration
     */
    public static class PHOENIX {
        @Comment("Phoenix Status Prefixes")
        public static String DEFAULT_STATUS = "&f";
        public static String VANISHED_PREFIX = "<#9e9e9e>[⚗] ";
        public static String MOD_MODE_PREFIX = "<#ffc430>[⚙] ";

        @Comment({
                "Rank Expiration Messages",
                "Message shown for permanent ranks"
        })
        public static String PERMANENT_RANK = "&aPermanent";
        public static String NO_TIME_REMAINING = "&c0s";

        /**
         * Rank expiration time unit configuration
         */
        public static class RANK_EXPIRY {
            @Comment({
                    "Time Unit Configuration",
                    "Set to false to cascade that unit into the next available unit",
                    "Example: If YEAR and MONTH are false, a '1 year 5 days' rank becomes '370 days'",
                    "Cascading order: Years -> Months -> Days -> Hours -> Minutes -> Seconds"
            })
            public static boolean YEAR = true;
            public static boolean MONTH = true;
            public static boolean DAY = true;
            public static boolean HOUR = true;
            public static boolean MINUTES = true;
            public static boolean SECONDS = true;

            @Comment("Singular Time Unit Labels")
            public static String YEAR_SINGULAR = " Year";
            public static String MONTH_SINGULAR = " Month";
            public static String DAY_SINGULAR = " Day";
            public static String HOUR_SINGULAR = " Hour";
            public static String MINUTE_SINGULAR = " Minute";
            public static String SECOND_SINGULAR = " Second";

            @Comment("Plural Time Unit Labels")
            public static String YEAR_PLURAL = " Years";
            public static String MONTH_PLURAL = " Months";
            public static String DAY_PLURAL = " Days";
            public static String HOUR_PLURAL = " Hours";
            public static String MINUTE_PLURAL = " Minutes";
            public static String SECOND_PLURAL = " Seconds";
        }
    }

    /**
     * Logger configuration
     */
    public static class LOGGER {
        @Comment("Logger Format Settings")
        public static String PREFIX = "&8[&6<plugin>&8]";
        public static String INFO_COLOR = "&b";
        public static String SUCCESS_COLOR = "&a";
        public static String ERROR_COLOR = "&c";
    }
}