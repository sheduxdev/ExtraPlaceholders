package dev.shedux.extraplaceholders.expansion;

import dev.shedux.extraplaceholders.ExtraPlaceholders;
import dev.shedux.extraplaceholders.handler.BoltPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.PhoenixPlaceholderHandler;
import dev.shedux.extraplaceholders.handler.ServerPlaceholderHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * PlaceholderAPI expansion for ExtraPlaceholders
 * Provides custom placeholders for server, Bolt, and Phoenix integrations
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class ExtraPlaceholdersExpansion extends PlaceholderExpansion {

    private final ExtraPlaceholders plugin;
    private final ServerPlaceholderHandler serverHandler;
    private final BoltPlaceholderHandler boltHandler;
    private final PhoenixPlaceholderHandler phoenixHandler;

    /**
     * Creates a new expansion instance
     *
     * @param plugin the main plugin instance
     */
    public ExtraPlaceholdersExpansion(ExtraPlaceholders plugin) {
        this.plugin = plugin;
        this.serverHandler = new ServerPlaceholderHandler();
        this.boltHandler = new BoltPlaceholderHandler();
        this.phoenixHandler = new PhoenixPlaceholderHandler();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "extraplaceholders";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Processes placeholder requests
     * Format: %extraplaceholders_<type>_<args>%
     *
     * @param player the player for whom the placeholder is being resolved
     * @param params the placeholder parameters
     * @return the resolved placeholder value, or null if not handled
     */
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        List<String> args = List.of(params.split("_"));

        if (args.isEmpty()) {
            return null;
        }

        String type = args.getFirst().toLowerCase();

        return switch (type) {
            case "server" -> serverHandler.handle(player, args);
            case "bolt" -> boltHandler.handle(player, args);
            case "phoenix" -> phoenixHandler.handle(player, args);
            default -> null;
        };
    }
}