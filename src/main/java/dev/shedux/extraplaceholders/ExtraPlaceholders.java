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

    private static final String PLACEHOLDERAPI_NAME = "PlaceholderAPI";
    private static final String PLUGIN_NAME = "ExtraPlaceholders";

    @Getter
    private static ExtraPlaceholders instance;

    private static Logger logger;
    private static ConfigHandler configHandler;

    private ExtraPlaceholdersExpansion expansion;

    /**
     * Called when the plugin is enabled
     * Initializes all components in proper order
     */
    @Override
    public void onEnable() {
        instance = this;
        logger = new Logger(PLUGIN_NAME);

        logPluginEnabled();

        if (!initializeConfiguration()) {
            disablePlugin();
            return;
        }

        initializeTrackers();

        if (!registerExpansion()) {
            disablePlugin();
            return;
        }

        registerCommands();
        logPluginReady();
    }

    /**
     * Called when the plugin is disabled
     * Cleans up resources
     */
    @Override
    public void onDisable() {
        unregisterExpansion();
        logPluginDisabled();
    }

    /**
     * Initializes the configuration system
     *
     * @return true if initialization successful
     */
    private boolean initializeConfiguration() {
        try {
            File folder = getDataFolder();
            configHandler = new ConfigHandler(getLogger());
            Configuration configuration = new Configuration(folder, configHandler);
            configuration.load();
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize configuration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initializes dependency trackers
     */
    private void initializeTrackers() {
        Initializer.initialize();
    }

    /**
     * Registers the PlaceholderAPI expansion
     *
     * @return true if registration successful
     */
    private boolean registerExpansion() {
        if (!isPlaceholderAPIPresent()) {
            logger.error(Configuration.MESSAGES.PLACEHOLDER_API_NOT_FOUND);
            return false;
        }

        try {
            expansion = new ExtraPlaceholdersExpansion(this);

            if (expansion.register()) {
                logger.success(Configuration.MESSAGES.EXPANSION_REGISTERED);
                return true;
            } else {
                logger.error(Configuration.MESSAGES.EXPANSION_FAILED);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to register expansion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Unregisters the PlaceholderAPI expansion
     */
    private void unregisterExpansion() {
        if (expansion != null) {
            try {
                expansion.unregister();
            } catch (Exception e) {
                logger.error("Failed to unregister expansion: " + e.getMessage());
            }
        }
    }

    /**
     * Registers plugin commands
     */
    private void registerCommands() {
        try {
            CommandHandler commandHandler = new BukkitCommandHandler(this);
            commandHandler.setLocale(createCommandLocale());
            commandHandler.register(new MainCommand(this));
        } catch (Exception e) {
            logger.error("Failed to register commands: " + e.getMessage());
        }
    }

    /**
     * Creates command locale with custom messages
     *
     * @return configured CommandLocale
     */
    private CommandLocale createCommandLocale() {
        return new CommandLocale() {
            @Override
            public List<String> getNoPermission() {
                return Collections.singletonList(
                        MessageUtil.colorize(Configuration.MESSAGES.NO_PERMISSION)
                );
            }
        };
    }

    /**
     * Checks if PlaceholderAPI is present
     *
     * @return true if PlaceholderAPI is loaded
     */
    private boolean isPlaceholderAPIPresent() {
        return getServer().getPluginManager().getPlugin(PLACEHOLDERAPI_NAME) != null;
    }

    /**
     * Disables the plugin
     */
    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Logs plugin enabled message
     */
    private void logPluginEnabled() {
        String message = Configuration.MESSAGES.PLUGIN_ENABLED
                .replace("<version>", getDescription().getVersion());
        logger.info(message);
    }

    /**
     * Logs plugin ready message
     */
    private void logPluginReady() {
        String message = Configuration.MESSAGES.PLUGIN_ENABLED
                .replace("<version>", getDescription().getVersion());
        logger.success(message);
    }

    /**
     * Logs plugin disabled message
     */
    private void logPluginDisabled() {
        logger.info(Configuration.MESSAGES.PLUGIN_DISABLED);
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