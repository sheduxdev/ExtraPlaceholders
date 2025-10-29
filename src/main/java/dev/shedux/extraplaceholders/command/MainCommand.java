package dev.shedux.extraplaceholders.command;

import dev.shedux.extraplaceholders.ExtraPlaceholders;
import dev.shedux.extraplaceholders.config.Configuration;
import dev.shedux.extraplaceholders.core.Initializer;
import dev.shedux.extraplaceholders.util.MessageUtil;
import net.j4c0b3y.api.command.annotation.command.Requires;
import net.j4c0b3y.api.command.annotation.parameter.classifier.Sender;
import net.j4c0b3y.api.command.annotation.registration.Register;
import org.bukkit.entity.Player;

/**
 * Main command handler for ExtraPlaceholders
 * Provides reload and info subcommands with proper error handling
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Register(name = "extraplaceholders", aliases = {"ep"}, description = "ExtraPlaceholders main command")
@Requires("extraplaceholders.admin")
@SuppressWarnings("unused")
public final class MainCommand {

    private final ExtraPlaceholders plugin;

    /**
     * Creates a new command instance
     *
     * @param plugin the main plugin instance
     */
    public MainCommand(ExtraPlaceholders plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads plugin configuration
     * Usage: /extraplaceholders reload
     *
     * @param player the command sender
     */
    @net.j4c0b3y.api.command.annotation.command.Command(name = "reload", description = "Reload plugin configuration")
    public void reload(@Sender Player player) {
        try {
            long duration = ExtraPlaceholders.reloadConfigurations();

            String message = Configuration.MESSAGES.RELOAD_SUCCESS
                    .replace("<duration>", String.valueOf(duration));

            MessageUtil.sendMessage(player, message);
        } catch (Exception e) {
            MessageUtil.sendMessage(player, Configuration.MESSAGES.RELOAD_ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Displays plugin information
     * Usage: /extraplaceholders info
     *
     * @param player the command sender
     */
    @net.j4c0b3y.api.command.annotation.command.Command(name = "info", description = "Show plugin information")
    public void info(@Sender Player player) {
        sendHeader(player);
        sendVersion(player);
        sendAuthor(player);
        sendBoltStatus(player);
        sendPhoenixStatus(player);
    }

    /**
     * Sends the info header
     *
     * @param player the command sender
     */
    private void sendHeader(Player player) {
        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_HEADER);
    }

    /**
     * Sends the plugin version
     *
     * @param player the command sender
     */
    private void sendVersion(Player player) {
        String message = Configuration.MESSAGES.PLUGIN_INFO_VERSION
                .replace("<version>", plugin.getDescription().getVersion());

        MessageUtil.sendMessage(player, message);
    }

    /**
     * Sends the plugin author(s)
     *
     * @param player the command sender
     */
    private void sendAuthor(Player player) {
        String authors = String.join(", ", plugin.getDescription().getAuthors());
        String message = Configuration.MESSAGES.PLUGIN_INFO_AUTHOR
                .replace("<author>", authors);

        MessageUtil.sendMessage(player, message);
    }

    /**
     * Sends Bolt dependency status
     *
     * @param player the command sender
     */
    private void sendBoltStatus(Player player) {
        String status = getDependencyStatus(Initializer.getBolt().isPresent());
        String message = Configuration.MESSAGES.PLUGIN_INFO_BOLT
                .replace("<status>", status);

        MessageUtil.sendMessage(player, message);
    }

    /**
     * Sends Phoenix dependency status
     *
     * @param player the command sender
     */
    private void sendPhoenixStatus(Player player) {
        String status = getDependencyStatus(Initializer.getPhoenix().isPresent());
        String message = Configuration.MESSAGES.PLUGIN_INFO_PHOENIX
                .replace("<status>", status);

        MessageUtil.sendMessage(player, message);
    }

    /**
     * Gets status message for dependency
     *
     * @param isPresent whether dependency is present
     * @return status message
     */
    private String getDependencyStatus(boolean isPresent) {
        return isPresent
                ? Configuration.MESSAGES.STATUS_ENABLED
                : Configuration.MESSAGES.STATUS_DISABLED;
    }
}