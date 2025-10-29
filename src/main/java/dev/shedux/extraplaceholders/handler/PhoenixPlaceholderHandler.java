package dev.shedux.extraplaceholders.handler;

import dev.shedux.extraplaceholders.config.Configuration;
import dev.shedux.extraplaceholders.core.Initializer;
import dev.shedux.extraplaceholders.tracker.PhoenixTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.BukkitAPI;
import xyz.refinedev.phoenix.profile.IProfile;
import xyz.refinedev.phoenix.profile.grant.IGrant;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Handles Phoenix-related placeholders for staff status information.
 *
 * <p>This handler provides functionality for:
 * <ul>
 *   <li>Vanish status detection</li>
 *   <li>Mod mode status detection</li>
 *   <li>Combined status display with configurable prefixes</li>
 *   <li>Rank expiration time with configurable time units</li>
 * </ul>
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public class PhoenixPlaceholderHandler implements PlaceholderHandler {

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        PhoenixTracker tracker = Initializer.getPhoenix();

        if (!isPhoenixAvailable(tracker)) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        if (args.size() > 1 && "status".equalsIgnoreCase(args.get(1))) {
            return getUserStatus(tracker, player);
        }

        if (args.size() > 1 && "expiration".equalsIgnoreCase(args.get(1))) {
            return getExpirationDate(tracker, player);
        }

        return null;
    }

    /**
     * Gets the formatted expiration date for the player's rank.
     *
     * @param tracker the Phoenix tracker
     * @param player the player
     * @return formatted expiration string
     */
    private String getExpirationDate(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player onlinePlayer)) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        IProfile profile = tracker.getApi().getProfileHandler().getProfile(onlinePlayer.getUniqueId());
        if (profile == null) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        IGrant grant = profile.getBestGrant();
        if (grant == null) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        long remainingMs = grant.getRemainingDuration();

        // Permanent rank
        if (remainingMs == -1 || remainingMs == Long.MAX_VALUE) {
            return Configuration.PHOENIX.PERMANENT_RANK;
        }

        return formatDuration(remainingMs);
    }

    /**
     * Formats duration based on configuration settings.
     * Cascades disabled units into the next available unit.
     *
     * @param durationMs duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long durationMs) {
        DurationComponents components = calculateDuration(durationMs);
        return buildDurationString(components);
    }

    /**
     * Calculates duration components with cascading logic.
     */
    private DurationComponents calculateDuration(long durationMs) {
        long remaining = durationMs;

        // Calculate base values
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        long totalHours = TimeUnit.MILLISECONDS.toHours(remaining);
        long totalDays = TimeUnit.MILLISECONDS.toDays(remaining);

        // Approximate months and years (30 days = 1 month, 365 days = 1 year)
        long totalMonths = totalDays / 30;
        long totalYears = totalDays / 365;

        // Start from the highest enabled unit and cascade down
        long years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

        if (Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            years = totalYears;
            remaining -= TimeUnit.DAYS.toMillis(years * 365);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MONTH) {
            months = TimeUnit.MILLISECONDS.toDays(remaining) / 30;
            remaining -= TimeUnit.DAYS.toMillis(months * 30);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            // If years are disabled, include years in months calculation
            months = totalMonths;
            remaining = durationMs - TimeUnit.DAYS.toMillis(months * 30);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.DAY) {
            days = TimeUnit.MILLISECONDS.toDays(remaining);
            remaining -= TimeUnit.DAYS.toMillis(days);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.MONTH && !Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            // If both years and months are disabled, show total days
            days = totalDays;
            remaining = durationMs - TimeUnit.DAYS.toMillis(days);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.HOUR) {
            hours = TimeUnit.MILLISECONDS.toHours(remaining);
            remaining -= TimeUnit.HOURS.toMillis(hours);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.DAY) {
            // If days are disabled, show total hours
            hours = totalHours;
            remaining = durationMs - TimeUnit.HOURS.toMillis(hours);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MINUTES) {
            minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
            remaining -= TimeUnit.MINUTES.toMillis(minutes);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.HOUR) {
            // If hours are disabled, show total minutes
            minutes = totalMinutes;
            remaining = durationMs - TimeUnit.MINUTES.toMillis(minutes);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.SECONDS) {
            seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.MINUTES) {
            // If minutes are disabled, show total seconds
            seconds = totalSeconds;
        }

        return new DurationComponents(years, months, days, hours, minutes, seconds);
    }

    /**
     * Builds the formatted duration string from components.
     */
    private String buildDurationString(DurationComponents components) {
        StringBuilder result = new StringBuilder();

        if (Configuration.PHOENIX.RANK_EXPIRY.YEAR && components.years > 0) {
            result.append(components.years)
                    .append(components.years == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.YEAR_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.YEAR_PLURAL)
                    .append(" ");
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MONTH && components.months > 0) {
            result.append(components.months)
                    .append(components.months == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.MONTH_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.MONTH_PLURAL)
                    .append(" ");
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.DAY && components.days > 0) {
            result.append(components.days)
                    .append(components.days == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.DAY_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.DAY_PLURAL)
                    .append(" ");
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.HOUR && components.hours > 0) {
            result.append(components.hours)
                    .append(components.hours == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.HOUR_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.HOUR_PLURAL)
                    .append(" ");
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MINUTES && components.minutes > 0) {
            result.append(components.minutes)
                    .append(components.minutes == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.MINUTE_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.MINUTE_PLURAL)
                    .append(" ");
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.SECONDS && components.seconds > 0) {
            result.append(components.seconds)
                    .append(components.seconds == 1 ?
                            Configuration.PHOENIX.RANK_EXPIRY.SECOND_SINGULAR :
                            Configuration.PHOENIX.RANK_EXPIRY.SECOND_PLURAL)
                    .append(" ");
        }

        String formatted = result.toString().trim();
        return formatted.isEmpty() ? Configuration.PHOENIX.NO_TIME_REMAINING : formatted;
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

    /**
     * Immutable record representing duration components.
     */
    private record DurationComponents(
            long years,
            long months,
            long days,
            long hours,
            long minutes,
            long seconds
    ) {}
}