package dev.shedux.extraplaceholders;

import dev.shedux.extraplaceholders.command.MainCommand;
import dev.shedux.extraplaceholders.config.Configuration;
import dev.shedux.extraplaceholders.core.Initializer;
import dev.shedux.extraplaceholders.expansion.ExtraPlaceholdersExpansion;
import dev.shedux.extraplaceholders.util.Logger;
import dev.shedux.extraplaceholders.util.MessageUtil;
import lombok.Getter;
import net.j4c0b3y.api.command.CommandHandler;
import net.j4c0b3y.api.command.bukkit.BukkitCommandHandler;
import net.j4c0b3y.api.command.execution.locale.CommandLocale;
import net.j4c0b3y.api.config.ConfigHandler;
import net.j4c0b3y.api.config.StaticConfig;
import org.apache.maven.model.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Main plugin class for ExtraPlaceholders
 * Provides custom placeholders for various plugin integrations
 *
 * @author sheduxdev
 * @version 1.0.0
 */
public final class ExtraPlaceholders extends JavaPlugin {

    @Getter
    private static ExtraPlaceholders instance;

    private static Logger logger;
    private static ConfigHandler configHandler;

    private ExtraPlaceholdersExpansion expansion;

    /**
     * Called when the plugin is enabled
     * Initializes configuration, trackers, expansion, and commands
     */
    @Override
    public void onEnable() {
        instance = this;
        logger = new Logger("ExtraPlaceholders");

        logger.info(Configuration.MESSAGES.PLUGIN_ENABLED
                .replace("<version>", getPluginMeta().getVersion()));

        initializeConfiguration();
        initializeTrackers();
        registerExpansion();
        registerCommands();

        logger.success(Configuration.MESSAGES.PLUGIN_ENABLED
                .replace("<version>", getPluginMeta().getVersion()));
    }

    /**
     * Called when the plugin is disabled
     * Unregisters the PlaceholderAPI expansion
     */
    @Override
    public void onDisable() {
        unregisterExpansion();
        logger.info(Configuration.MESSAGES.PLUGIN_DISABLED);
    }

    /**
     * Initializes the configuration system
     */
    private void initializeConfiguration() {
        File folder = getDataFolder();
        configHandler = new ConfigHandler(getLogger());
        Configuration configuration = new Configuration(folder, configHandler);
        configuration.load();
    }

    /**
     * Initializes dependency trackers
     */
    private void initializeTrackers() {
        Initializer.initialize();
    }

    /**
     * Registers the PlaceholderAPI expansion
     */
    private void registerExpansion() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.error(Configuration.MESSAGES.PLACEHOLDER_API_NOT_FOUND);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        expansion = new ExtraPlaceholdersExpansion(this);

        if (expansion.register()) {
            logger.success(Configuration.MESSAGES.EXPANSION_REGISTERED);
        } else {
            logger.error(Configuration.MESSAGES.EXPANSION_FAILED);
        }
    }

    /**
     * Unregisters the PlaceholderAPI expansion
     */
    private void unregisterExpansion() {
        if (expansion != null) {
            expansion.unregister();
        }
    }

    /**
     * Registers plugin commands
     */
    private void registerCommands() {
        CommandHandler commandHandler = new BukkitCommandHandler(this);
        commandHandler.setLocale(new CommandLocale() {
            @Override
            public List<String> getNoPermission() {
                return Collections.singletonList(MessageUtil.colorize(Configuration.MESSAGES.NO_PERMISSION));
            }
        });
        commandHandler.register(new MainCommand(this));
    }

    /**
     * Reloads all registered configurations
     *
     * @return reload duration in milliseconds
     */
    public static long reloadConfigurations() {
        long startTime = System.currentTimeMillis();
        configHandler.getRegistered().forEach(StaticConfig::load);
        return System.currentTimeMillis() - startTime;
    }
}