package dev.shedux.extraplaceholders.handler;

import dev.shedux.extraplaceholders.config.Configuration;
import dev.shedux.extraplaceholders.core.Initializer;
import dev.shedux.extraplaceholders.tracker.PhoenixTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.BukkitAPI;
import xyz.refinedev.phoenix.profile.IProfile;

import java.util.List;
import java.util.Optional;

/**
 * Handles Phoenix-related placeholders for staff status information.
 *
 * <p>This handler provides functionality for:
 * <ul>
 *   <li>Vanish status detection</li>
 *   <li>Mod mode status detection</li>
 *   <li>Combined status display with configurable prefixes</li>
 * </ul>
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class PhoenixPlaceholderHandler implements PlaceholderHandler {

    private static final String STATUS_COMMAND = "status";

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        PhoenixTracker tracker = Initializer.getPhoenix();

        if (!isPhoenixAvailable(tracker)) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        if (args.size() > 1 && STATUS_COMMAND.equalsIgnoreCase(args.get(1))) {
            return getUserStatus(tracker, player);
        }

        return null;
    }

    /**
     * Checks if Phoenix API is available.
     */
    private boolean isPhoenixAvailable(PhoenixTracker tracker) {
        return tracker.isPresent() && tracker.getApi() != null;
    }

    /**
     * Gets the user status including vanished and mod mode indicators.
     *
     * @param tracker the Phoenix tracker
     * @param player the player
     * @return formatted status string with prefixes
     */
    private String getUserStatus(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player onlinePlayer)) {
            return Configuration.PHOENIX.DEFAULT_STATUS;
        }

        return getPlayerProfile(tracker, player)
                .map(profile -> buildStatusString(profile, onlinePlayer))
                .orElse(Configuration.PHOENIX.DEFAULT_STATUS);
    }

    /**
     * Gets the player profile from Phoenix API.
     */
    private Optional<IProfile> getPlayerProfile(PhoenixTracker tracker, OfflinePlayer player) {
        try {
            IProfile profile = tracker.getApi()
                    .getProfileHandler()
                    .getProfile(player.getUniqueId());
            return Optional.ofNullable(profile);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Builds the status string with appropriate prefixes.
     *
     * @param profile the player profile
     * @param player the online player
     * @return formatted status string with prefixes
     */
    private String buildStatusString(IProfile profile, Player player) {
        PlayerStatus status = determinePlayerStatus(profile, player);

        if (!status.hasAnyStatus()) {
            return Configuration.PHOENIX.DEFAULT_STATUS;
        }

        return buildPrefixString(status);
    }

    /**
     * Determines the player's current status.
     */
    private PlayerStatus determinePlayerStatus(IProfile profile, Player player) {
        boolean isVanished = profile.isVanished();
        boolean isModMode = checkModMode(player);
        return new PlayerStatus(isVanished, isModMode);
    }

    /**
     * Checks if player is in mod mode safely.
     */
    private boolean checkModMode(Player player) {
        try {
            return BukkitAPI.isInModMode(player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Builds the prefix string based on status.
     */
    private String buildPrefixString(PlayerStatus status) {
        StringBuilder builder = new StringBuilder();

        if (status.isVanished()) {
            builder.append(Configuration.PHOENIX.VANISHED_PREFIX);
        }

        if (status.isModMode()) {
            builder.append(Configuration.PHOENIX.MOD_MODE_PREFIX);
        }

        return builder.toString();
    }

    /**
     * Immutable record representing player status.
     */
    private record PlayerStatus(boolean isVanished, boolean isModMode) {
        boolean hasAnyStatus() {
            return isVanished || isModMode;
        }
    }
}