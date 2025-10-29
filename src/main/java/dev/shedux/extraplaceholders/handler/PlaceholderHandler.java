package dev.shedux.extraplaceholders.handler;

import org.bukkit.OfflinePlayer;
import java.util.List;

/**
 * Base interface for all placeholder handlers
 * Ensures consistent placeholder processing API across all handlers
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public interface PlaceholderHandler {

    /**
     * Handles placeholder requests with validation
     *
     * @param player the player for whom the placeholder is being resolved
     * @param args the placeholder arguments (never null or empty)
     * @return the resolved placeholder value, or null if not handled
     */
    String handle(OfflinePlayer player, List<String> args);

    /**
     * Gets the handler identifier for routing
     * Derived from the class name by default
     *
     * @return the handler type identifier
     */
    default String getHandlerType() {
        return this.getClass().getSimpleName()
                .replace("PlaceholderHandler", "")
                .toLowerCase();
    }

    /**
     * Validates if the handler has minimum required arguments
     *
     * @param args the arguments to validate
     * @param minSize the minimum required size
     * @return true if arguments meet minimum requirements
     */
    default boolean hasMinimumArgs(List<String> args, int minSize) {
        return args != null && args.size() >= minSize;
    }
}