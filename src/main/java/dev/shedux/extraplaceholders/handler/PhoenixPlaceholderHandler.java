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
 * Handles Phoenix-related placeholders for staff status and rank information
 * Provides vanish status, mod mode detection, and rank expiration tracking
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public final class PhoenixPlaceholderHandler implements PlaceholderHandler {

    private static final String STATUS_COMMAND = "status";
    private static final String EXPIRATION_COMMAND = "expiration";
    private static final int MIN_ARGS = 2;
    private static final int COMMAND_INDEX = 1;

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        PhoenixTracker tracker = Initializer.getPhoenix();

        if (!tracker.isApiAvailable()) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        if (!hasMinimumArgs(args, MIN_ARGS)) {
            return null;
        }

        String command = args.get(COMMAND_INDEX).toLowerCase();

        return switch (command) {
            case STATUS_COMMAND -> handleStatusPlaceholder(tracker, player);
            case EXPIRATION_COMMAND -> handleExpirationPlaceholder(tracker, player);
            default -> null;
        };
    }

    /**
     * Handles status placeholder (vanish/mod mode indicators)
     *
     * @param tracker the Phoenix tracker
     * @param player the player
     * @return formatted status string with prefixes
     */
    private String handleStatusPlaceholder(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player onlinePlayer)) {
            return Configuration.PHOENIX.DEFAULT_STATUS;
        }

        return getPlayerProfile(tracker, player)
                .map(profile -> buildStatusString(profile, onlinePlayer))
                .orElse(Configuration.PHOENIX.DEFAULT_STATUS);
    }

    /**
     * Handles rank expiration placeholder
     *
     * @param tracker the Phoenix tracker
     * @param player the player
     * @return formatted expiration string
     */
    private String handleExpirationPlaceholder(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player)) {
            return Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE;
        }

        return getPlayerProfile(tracker, player)
                .flatMap(this::getBestGrant)
                .map(this::formatGrantExpiration)
                .orElse(Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE);
    }

    /**
     * Gets the player profile from Phoenix API safely
     *
     * @param tracker the Phoenix tracker
     * @param player the player
     * @return Optional containing the profile if found
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
     * Gets the best grant from profile safely
     *
     * @param profile the player profile
     * @return Optional containing the grant if found
     */
    private Optional<IGrant> getBestGrant(IProfile profile) {
        try {
            return Optional.ofNullable(profile.getBestGrant());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Formats grant expiration time
     *
     * @param grant the grant to format
     * @return formatted expiration string
     */
    private String formatGrantExpiration(IGrant grant) {
        long remainingMs = grant.getRemainingDuration();

        if (isPermanentGrant(remainingMs)) {
            return Configuration.PHOENIX.PERMANENT_RANK;
        }

        return formatDuration(remainingMs);
    }

    /**
     * Checks if grant is permanent
     *
     * @param remainingMs remaining time in milliseconds
     * @return true if grant is permanent
     */
    private boolean isPermanentGrant(long remainingMs) {
        return remainingMs == -1 || remainingMs == Long.MAX_VALUE;
    }

    /**
     * Builds the status string with appropriate prefixes
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
     * Determines the player's current status
     *
     * @param profile the player profile
     * @param player the online player
     * @return PlayerStatus object containing status flags
     */
    private PlayerStatus determinePlayerStatus(IProfile profile, Player player) {
        boolean isVanished = isPlayerVanished(profile);
        boolean isModMode = isPlayerInModMode(player);
        return new PlayerStatus(isVanished, isModMode);
    }

    /**
     * Checks if player is vanished safely
     *
     * @param profile the player profile
     * @return true if player is vanished
     */
    private boolean isPlayerVanished(IProfile profile) {
        try {
            return profile.isVanished();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if player is in mod mode safely
     *
     * @param player the player
     * @return true if player is in mod mode
     */
    private boolean isPlayerInModMode(Player player) {
        try {
            return BukkitAPI.isInModMode(player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Builds the prefix string based on status
     *
     * @param status the player status
     * @return formatted prefix string
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
     * Formats duration based on configuration settings with cascading
     *
     * @param durationMs duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long durationMs) {
        DurationComponents components = calculateDuration(durationMs);
        return buildDurationString(components);
    }

    /**
     * Calculates duration components with cascading logic
     *
     * @param durationMs duration in milliseconds
     * @return DurationComponents containing all time units
     */
    private DurationComponents calculateDuration(long durationMs) {
        long remaining = durationMs;

        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        long totalHours = TimeUnit.MILLISECONDS.toHours(remaining);
        long totalDays = TimeUnit.MILLISECONDS.toDays(remaining);
        long totalMonths = totalDays / 30;
        long totalYears = totalDays / 365;

        long years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

        if (Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            years = totalYears;
            remaining -= TimeUnit.DAYS.toMillis(years * 365);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MONTH) {
            months = TimeUnit.MILLISECONDS.toDays(remaining) / 30;
            remaining -= TimeUnit.DAYS.toMillis(months * 30);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            months = totalMonths;
            remaining = durationMs - TimeUnit.DAYS.toMillis(months * 30);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.DAY) {
            days = TimeUnit.MILLISECONDS.toDays(remaining);
            remaining -= TimeUnit.DAYS.toMillis(days);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.MONTH && !Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            days = totalDays;
            remaining = durationMs - TimeUnit.DAYS.toMillis(days);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.HOUR) {
            hours = TimeUnit.MILLISECONDS.toHours(remaining);
            remaining -= TimeUnit.HOURS.toMillis(hours);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.DAY) {
            hours = totalHours;
            remaining = durationMs - TimeUnit.HOURS.toMillis(hours);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.MINUTES) {
            minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
            remaining -= TimeUnit.MINUTES.toMillis(minutes);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.HOUR) {
            minutes = totalMinutes;
            remaining = durationMs - TimeUnit.MINUTES.toMillis(minutes);
        }

        if (Configuration.PHOENIX.RANK_EXPIRY.SECONDS) {
            seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
        } else if (!Configuration.PHOENIX.RANK_EXPIRY.MINUTES) {
            seconds = totalSeconds;
        }

        return new DurationComponents(years, months, days, hours, minutes, seconds);
    }

    /**
     * Builds the formatted duration string from components
     *
     * @param components the duration components
     * @return formatted duration string
     */
    private String buildDurationString(DurationComponents components) {
        StringBuilder result = new StringBuilder();

        appendTimeUnit(result, components.years(), Configuration.PHOENIX.RANK_EXPIRY.YEAR,
                Configuration.PHOENIX.RANK_EXPIRY.YEAR_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.YEAR_PLURAL);

        appendTimeUnit(result, components.months(), Configuration.PHOENIX.RANK_EXPIRY.MONTH,
                Configuration.PHOENIX.RANK_EXPIRY.MONTH_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.MONTH_PLURAL);

        appendTimeUnit(result, components.days(), Configuration.PHOENIX.RANK_EXPIRY.DAY,
                Configuration.PHOENIX.RANK_EXPIRY.DAY_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.DAY_PLURAL);

        appendTimeUnit(result, components.hours(), Configuration.PHOENIX.RANK_EXPIRY.HOUR,
                Configuration.PHOENIX.RANK_EXPIRY.HOUR_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.HOUR_PLURAL);

        appendTimeUnit(result, components.minutes(), Configuration.PHOENIX.RANK_EXPIRY.MINUTES,
                Configuration.PHOENIX.RANK_EXPIRY.MINUTE_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.MINUTE_PLURAL);

        appendTimeUnit(result, components.seconds(), Configuration.PHOENIX.RANK_EXPIRY.SECONDS,
                Configuration.PHOENIX.RANK_EXPIRY.SECOND_SINGULAR,
                Configuration.PHOENIX.RANK_EXPIRY.SECOND_PLURAL);

        String formatted = result.toString().trim();
        return formatted.isEmpty() ? Configuration.PHOENIX.NO_TIME_REMAINING : formatted;
    }

    /**
     * Appends a time unit to the result string if enabled and non-zero
     *
     * @param result the StringBuilder to append to
     * @param value the time unit value
     * @param enabled whether this unit is enabled in config
     * @param singularLabel the singular form label
     * @param pluralLabel the plural form label
     */
    private void appendTimeUnit(StringBuilder result, long value, boolean enabled,
                                String singularLabel, String pluralLabel) {
        if (enabled && value > 0) {
            result.append(value)
                    .append(value == 1 ? singularLabel : pluralLabel)
                    .append(" ");
        }
    }

    /**
     * Immutable record representing player status flags
     */
    private record PlayerStatus(boolean isVanished, boolean isModMode) {
        boolean hasAnyStatus() {
            return isVanished || isModMode;
        }
    }

    /**
     * Immutable record representing duration components
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