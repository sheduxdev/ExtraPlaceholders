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
 * Provides reload and info subcommands
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Register(name = "extraplaceholders", aliases = {"ep"}, description = "ExtraPlaceholders main command")
@Requires("extraplaceholders.admin")
@SuppressWarnings("unused")
public class MainCommand {

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
            MessageUtil.sendMessage(player, Configuration.MESSAGES.RELOAD_SUCCESS
                    .replace("<duration>", String.valueOf(duration)));
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
        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_HEADER);

        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_VERSION
                .replace("<version>", plugin.getPluginMeta().getVersion()));

        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_AUTHOR
                .replace("<author>", String.join(", ", plugin.getPluginMeta().getAuthors())));

        String boltStatus = Initializer.getBolt().isPresent()
                ? Configuration.MESSAGES.STATUS_ENABLED
                : Configuration.MESSAGES.STATUS_DISABLED;
        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_BOLT
                .replace("<status>", boltStatus));

        String phoenixStatus = Initializer.getPhoenix().isPresent()
                ? Configuration.MESSAGES.STATUS_ENABLED
                : Configuration.MESSAGES.STATUS_DISABLED;
        MessageUtil.sendMessage(player, Configuration.MESSAGES.PLUGIN_INFO_PHOENIX
                .replace("<status>", phoenixStatus));
    }
}