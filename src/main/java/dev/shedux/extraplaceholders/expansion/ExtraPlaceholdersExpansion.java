package dev.shedux.extraplaceholders.expansion;

import dev.shedux.extraplaceholders.ExtraPlaceholders;
import dev.shedux.extraplaceholders.handler.BoltPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.PhoenixPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.PlaceholderHandler;
import dev.shedux.extraplaceholders.handler.ServerPlaceholderHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ExtraPlaceholdersExpansion extends PlaceholderExpansion {

    private static final String IDENTIFIER = "extraplaceholders";
    private static final String PARAM_SEPARATOR = "_";
    private static final int HANDLER_TYPE_INDEX = 0;

    private final ExtraPlaceholders plugin;
    private final Map<String, PlaceholderHandler> handlers;

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

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<String> args = parseParameters(params);

        if (args.isEmpty()) {
            return null;
        }

        String handlerType = getHandlerType(args);
        PlaceholderHandler handler = getHandler(handlerType);

        if (handler == null) {
            return null; // Hata mesajı yerine null dön
        }

        try {
            String result = handler.handle(player, args);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> parseParameters(String params) {
        String[] split = params.split(PARAM_SEPARATOR);
        return new ArrayList<>(Arrays.asList(split));
    }

    private String getHandlerType(List<String> args) {
        return args.get(HANDLER_TYPE_INDEX).toLowerCase();
    }

    private PlaceholderHandler getHandler(String handlerType) {
        return handlers.get(handlerType);
    }

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