package dev.shedux.extraplaceholders.expansion;

import dev.shedux.extraplaceholders.ExtraPlaceholders;
import dev.shedux.extraplaceholders.handler.BoltPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.PhoenixPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.PlaceholderHandler;
import dev.shedux.extraplaceholders.handler.ServerPlaceholderHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * PlaceholderAPI expansion for ExtraPlaceholders
 * Routes placeholder requests to appropriate handlers
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public final class ExtraPlaceholdersExpansion extends PlaceholderExpansion {

    private static final String IDENTIFIER = "extraplaceholders";
    private static final int MIN_PARAMS_LENGTH = 2;
    private static final String PARAM_SEPARATOR = "_";
    private static final int HANDLER_TYPE_INDEX = 1;

    private final ExtraPlaceholders plugin;
    private final Map<String, PlaceholderHandler> handlers;

    /**
     * Creates a new expansion instance with all handlers
     *
     * @param plugin the main plugin instance
     */
    public ExtraPlaceholdersExpansion(ExtraPlaceholders plugin) {
        this.plugin = plugin;
        this.handlers = initializeHandlers();
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Processes placeholder requests and routes to appropriate handler
     * Format: %extraplaceholders_<type>_<args>%
     *
     * @param player the player for whom the placeholder is being resolved
     * @param params the placeholder parameters
     * @return the resolved placeholder value, or null if not handled
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        List<String> args = parseParameters(params);

        if (!hasMinimumParameters(args)) {
            return null;
        }

        String handlerType = getHandlerType(args);
        PlaceholderHandler handler = getHandler(handlerType);

        if (handler == null) {
            return null;
        }

        return handler.handle(player, args);
    }

    /**
     * Parses parameter string into list
     *
     * @param params the parameter string
     * @return list of parameters
     */
    private List<String> parseParameters(String params) {
        return List.of(params.split(PARAM_SEPARATOR));
    }

    /**
     * Checks if parameters meet minimum requirements
     *
     * @param args the parameter list
     * @return true if minimum requirements met
     */
    private boolean hasMinimumParameters(List<String> args) {
        return args != null && args.size() >= MIN_PARAMS_LENGTH;
    }

    /**
     * Extracts handler type from parameters
     *
     * @param args the parameter list
     * @return handler type identifier
     */
    private String getHandlerType(List<String> args) {
        return args.get(HANDLER_TYPE_INDEX).toLowerCase();
    }

    /**
     * Gets handler for specified type
     *
     * @param handlerType the handler type
     * @return the handler or null if not found
     */
    private PlaceholderHandler getHandler(String handlerType) {
        return handlers.get(handlerType);
    }

    /**
     * Initializes all placeholder handlers
     *
     * @return immutable map of handler types to instances
     */
    private Map<String, PlaceholderHandler> initializeHandlers() {
        ServerPlaceholderHandler serverHandler = new ServerPlaceholderHandler();
        BoltPlaceholderHandler boltHandler = new BoltPlaceholderHandler();
        PhoenixPlaceholderHandler phoenixHandler = new PhoenixPlaceholderHandler();

        return Map.of(
                serverHandler.getHandlerType(), serverHandler,
                boltHandler.getHandlerType(), boltHandler,
                phoenixHandler.getHandlerType(), phoenixHandler
        );
    }
}