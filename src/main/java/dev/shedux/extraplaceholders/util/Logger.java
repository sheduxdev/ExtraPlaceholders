package dev.shedux.extraplaceholders.util;

import dev.shedux.extraplaceholders.config.Configuration;
import org.bukkit.ChatColor;

/**
 * Custom logger for plugin messages with configurable formatting
 * Provides consistent logging with color-coded message types
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public final class Logger {

    private final String pluginName;

    /**
     * Creates a new logger instance
     *
     * @param pluginName the name of the plugin for log prefix
     */
    public Logger(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Logs an info message
     *
     * @param message the message to log
     */
    public void info(String message) {
        log(Configuration.LOGGER.INFO_COLOR, message);
    }

    /**
     * Logs a success message
     *
     * @param message the message to log
     */
    public void success(String message) {
        log(Configuration.LOGGER.SUCCESS_COLOR, message);
    }

    /**
     * Logs an error message
     *
     * @param message the message to log
     */
    public void error(String message) {
        log(Configuration.LOGGER.ERROR_COLOR, message);
    }

    /**
     * Internal log method with color formatting
     *
     * @param colorCode the color code for the message
     * @param message the message to log
     */
    private void log(String colorCode, String message) {
        String prefix = buildPrefix();
        String formatted = String.format("%s %s%s", prefix, colorCode, message);
        System.out.println(colorize(formatted));
    }

    /**
     * Builds the log prefix with plugin name
     *
     * @return formatted prefix string
     */
    private String buildPrefix() {
        return Configuration.LOGGER.PREFIX.replace("<plugin>", pluginName);
    }

    /**
     * Colorizes a message with legacy color codes
     *
     * @param message the message to colorize
     * @return colorized message
     */
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}