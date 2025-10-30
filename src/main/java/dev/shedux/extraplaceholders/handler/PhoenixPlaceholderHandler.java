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

public final class PhoenixPlaceholderHandler implements PlaceholderHandler {

    private static final String STATUS_COMMAND = "status";
    private static final String EXPIRATION_COMMAND = "expiration";
    private static final int MIN_ARGS = 2;
    private static final int COMMAND_INDEX = 1;

    @Override
    public String handle(OfflinePlayer player, List<String> args) {
        PhoenixTracker tracker = Initializer.getPhoenix();

        if (!tracker.isApiAvailable()) {
            return cleanForScoreboard(Configuration.MESSAGES.PHOENIX_NOT_AVAILABLE);
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

    private String handleStatusPlaceholder(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player onlinePlayer)) {
            return cleanForScoreboard(Configuration.PHOENIX.DEFAULT_STATUS);
        }

        return getPlayerProfile(tracker, player)
                .map(profile -> buildStatusString(profile, onlinePlayer))
                .orElse(cleanForScoreboard(Configuration.PHOENIX.DEFAULT_STATUS));
    }

    private String handleExpirationPlaceholder(PhoenixTracker tracker, OfflinePlayer player) {
        if (!(player instanceof Player)) {
            return cleanForScoreboard(Configuration.PHOENIX.PERMANENT_RANK);
        }

        return getPlayerProfile(tracker, player)
                .flatMap(this::getBestGrant)
                .map(this::formatGrantExpiration)
                .orElse(cleanForScoreboard(Configuration.PHOENIX.PERMANENT_RANK));
    }

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

    private Optional<IGrant> getBestGrant(IProfile profile) {
        try {
            IGrant grant = profile.getBestGrant();
            return Optional.ofNullable(grant);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String formatGrantExpiration(IGrant grant) {
        long remainingMs = grant.getRemainingDuration();

        if (isPermanentGrant(remainingMs)) {
            return cleanForScoreboard(Configuration.PHOENIX.PERMANENT_RANK);
        }

        if (remainingMs <= 0) {
            return cleanForScoreboard(Configuration.PHOENIX.PERMANENT_RANK);
        }

        return formatDuration(remainingMs);
    }

    private boolean isPermanentGrant(long remainingMs) {
        return remainingMs == -1 ||
                remainingMs == Long.MAX_VALUE ||
                remainingMs <= 0 ||
                remainingMs > TimeUnit.DAYS.toMillis(36500);
    }

    private String buildStatusString(IProfile profile, Player player) {
        PlayerStatus status = determinePlayerStatus(profile, player);

        if (!status.hasAnyStatus()) {
            return cleanForScoreboard(Configuration.PHOENIX.DEFAULT_STATUS);
        }

        return buildPrefixString(status);
    }

    private PlayerStatus determinePlayerStatus(IProfile profile, Player player) {
        boolean isVanished = isPlayerVanished(profile);
        boolean isModMode = isPlayerInModMode(player);
        return new PlayerStatus(isVanished, isModMode);
    }

    private boolean isPlayerVanished(IProfile profile) {
        try {
            return profile.isVanished();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPlayerInModMode(Player player) {
        try {
            return BukkitAPI.isInModMode(player);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildPrefixString(PlayerStatus status) {
        StringBuilder builder = new StringBuilder();

        if (status.isVanished()) {
            builder.append(cleanForScoreboard(Configuration.PHOENIX.VANISHED_PREFIX));
        }

        if (status.isModMode()) {
            builder.append(cleanForScoreboard(Configuration.PHOENIX.MOD_MODE_PREFIX));
        }

        return builder.toString();
    }

    private String formatDuration(long durationMs) {
        DurationComponents components = calculateDuration(durationMs);
        String result = buildDurationString(components);

        // Scoreboard uyumluluğu için temizle
        result = cleanForScoreboard(result);

        return result.isEmpty() ?
                cleanForScoreboard(Configuration.PHOENIX.NO_TIME_REMAINING) :
                result;
    }

    private DurationComponents calculateDuration(long durationMs) {
        long years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
        long remaining = durationMs;

        // Yılları hesapla (eğer aktifse)
        if (Configuration.PHOENIX.RANK_EXPIRY.YEAR) {
            years = TimeUnit.MILLISECONDS.toDays(remaining) / 365;
            remaining -= TimeUnit.DAYS.toMillis(years * 365);
        }

        // Ayları hesapla (eğer aktifse)
        if (Configuration.PHOENIX.RANK_EXPIRY.MONTH) {
            months = TimeUnit.MILLISECONDS.toDays(remaining) / 30;
            remaining -= TimeUnit.DAYS.toMillis(months * 30);
        }

        // Günleri hesapla
        days = TimeUnit.MILLISECONDS.toDays(remaining);
        remaining -= TimeUnit.DAYS.toMillis(days);

        // Saatleri hesapla
        hours = TimeUnit.MILLISECONDS.toHours(remaining);
        remaining -= TimeUnit.HOURS.toMillis(hours);

        // Dakikaları hesapla
        minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        remaining -= TimeUnit.MINUTES.toMillis(minutes);

        // Saniyeleri hesapla
        seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);

        // Eğer yıl ve ay kapalıysa, kalan süreyi günlere ekle
        if (!Configuration.PHOENIX.RANK_EXPIRY.YEAR && !Configuration.PHOENIX.RANK_EXPIRY.MONTH) {
            // Önceki hesaplamaları sıfırla ve yeniden hesapla
            days = TimeUnit.MILLISECONDS.toDays(durationMs);
            long remainingAfterDays = durationMs - TimeUnit.DAYS.toMillis(days);

            hours = TimeUnit.MILLISECONDS.toHours(remainingAfterDays);
            long remainingAfterHours = remainingAfterDays - TimeUnit.HOURS.toMillis(hours);

            minutes = TimeUnit.MILLISECONDS.toMinutes(remainingAfterHours);
            long remainingAfterMinutes = remainingAfterHours - TimeUnit.MINUTES.toMillis(minutes);

            seconds = TimeUnit.MILLISECONDS.toSeconds(remainingAfterMinutes);
        }
        // Eğer sadece yıl kapalıysa
        else if (!Configuration.PHOENIX.RANK_EXPIRY.YEAR && Configuration.PHOENIX.RANK_EXPIRY.MONTH) {
            // Ayları ve günleri yeniden hesapla
            long totalDays = TimeUnit.MILLISECONDS.toDays(durationMs);
            months = totalDays / 30;
            days = totalDays % 30;

            long remainingAfterDays = durationMs - TimeUnit.DAYS.toMillis(totalDays);
            hours = TimeUnit.MILLISECONDS.toHours(remainingAfterDays);
            long remainingAfterHours = remainingAfterDays - TimeUnit.HOURS.toMillis(hours);

            minutes = TimeUnit.MILLISECONDS.toMinutes(remainingAfterHours);
            long remainingAfterMinutes = remainingAfterHours - TimeUnit.MINUTES.toMillis(minutes);

            seconds = TimeUnit.MILLISECONDS.toSeconds(remainingAfterMinutes);
        }

        return new DurationComponents(years, months, days, hours, minutes, seconds);
    }

    private String buildDurationString(DurationComponents components) {
        StringBuilder result = new StringBuilder();
        boolean hasContent = false;

        // Yıl ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.YEAR && components.years() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.years());
            result.append(components.years() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.YEAR_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.YEAR_PLURAL);
            hasContent = true;
        }

        // Ay ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.MONTH && components.months() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.months());
            result.append(components.months() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.MONTH_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.MONTH_PLURAL);
            hasContent = true;
        }

        // Gün ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.DAY && components.days() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.days());
            result.append(components.days() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.DAY_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.DAY_PLURAL);
            hasContent = true;
        }

        // Saat ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.HOUR && components.hours() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.hours());
            result.append(components.hours() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.HOUR_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.HOUR_PLURAL);
            hasContent = true;
        }

        // Dakika ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.MINUTES && components.minutes() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.minutes());
            result.append(components.minutes() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.MINUTE_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.MINUTE_PLURAL);
            hasContent = true;
        }

        // Saniye ekle (eğer aktifse ve değer > 0)
        if (Configuration.PHOENIX.RANK_EXPIRY.SECONDS && components.seconds() > 0) {
            if (hasContent) result.append(" ");
            result.append(components.seconds());
            result.append(components.seconds() == 1 ?
                    Configuration.PHOENIX.RANK_EXPIRY.SECOND_SINGULAR :
                    Configuration.PHOENIX.RANK_EXPIRY.SECOND_PLURAL);
            hasContent = true;
        }

        String finalResult = result.toString().trim();

        // Eğer hiçbir şey eklenmediyse "No time" döndür
        return finalResult.isEmpty() ? Configuration.PHOENIX.NO_TIME_REMAINING : finalResult;
    }

    /**
     * Scoreboard'da görüntülenebilmesi için metni temizler
     * MiniMessage formatını legacy color codes'a çevirir ve gereksiz karakterleri kaldırır
     */
    private String cleanForScoreboard(String text) {
        if (text == null) return "";

        // MiniMessage hex formatını kaldır
        text = text.replaceAll("<#[A-Fa-f0-9]{6}>", "");
        text = text.replaceAll("</gradient>", "");
        text = text.replaceAll("<gradient:#[A-Fa-f0-9]{6}:#[A-Fa-f0-9]{6}>", "");

        // Legacy color codes korunsun, sadece temizle
        text = text.replace("&", "§");

        return text.trim();
    }

    private record PlayerStatus(boolean isVanished, boolean isModMode) {
        boolean hasAnyStatus() {
            return isVanished || isModMode;
        }
    }

    private record DurationComponents(
            long years,
            long months,
            long days,
            long hours,
            long minutes,
            long seconds
    ) {}
}