package dev.shedux.extraplaceholders.util;

import dev.shedux.extraplaceholders.config.Configuration;
import org.bukkit.ChatColor;

/**
 * Custom logger for plugin messages
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class Logger {

    private final String pluginName;

    /**
     * Creates a new logger instance
     *
     * @param pluginName the name of the plugin
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
     * Internal log method
     *
     * @param color the color code
     * @param message the message to log
     */
    private void log(String color, String message) {
        String prefix = Configuration.LOGGER.PREFIX.replace("<plugin>", pluginName);
        String formatted = String.format("%s %s%s", prefix, color, message);
        System.out.println(colorize(formatted));
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