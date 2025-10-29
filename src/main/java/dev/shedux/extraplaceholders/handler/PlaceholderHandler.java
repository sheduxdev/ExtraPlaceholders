package dev.shedux.extraplaceholders.handler;

import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Interface for placeholder handlers
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public interface PlaceholderHandler {

    /**
     * Handles placeholder requests
     *
     * @param player the player for whom the placeholder is being resolved
     * @param args the placeholder arguments
     * @return the resolved placeholder value, or null if not handled
     */
    String handle(OfflinePlayer player, List<String> args);
}