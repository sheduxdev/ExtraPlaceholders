package dev.shedux.extraplaceholders.tracker;

import dev.shedux.extraplaceholders.api.PluginDependency;
import lombok.Getter;
import xyz.refinedev.phoenix.CommonPlatform;
import xyz.refinedev.phoenix.Phoenix;
import xyz.refinedev.phoenix.PlatformGetter;

/**
 * Tracker for Phoenix plugin dependency
 * Manages Phoenix API and platform availability with safe initialization
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Getter
public final class PhoenixTracker implements PluginDependency {

    private static final String DEPENDENCY_NAME = "Phoenix";

    private final boolean present;
    private final Phoenix api;
    private final CommonPlatform commonPlatform;

    /**
     * Initializes the Phoenix tracker with safe API access
     * Handles missing dependencies gracefully without throwing exceptions
     */
    public PhoenixTracker() {
        Phoenix tempApi = null;
        CommonPlatform tempPlatform = null;
        boolean isPresent = false;

        try {
            tempPlatform = PlatformGetter.getInstance();
        } catch (NoClassDefFoundError | Exception ignored) {
            // Phoenix platform not available - fail silently
        }

        try {
            tempApi = Phoenix.INSTANCE;
            isPresent = (tempApi != null);
        } catch (NoClassDefFoundError | Exception ignored) {
            // Phoenix API not available - fail silently
        }

        this.commonPlatform = tempPlatform;
        this.api = tempApi;
        this.present = isPresent;
    }

    @Override
    public String getDependencyName() {
        return DEPENDENCY_NAME;
    }

    @Override
    public boolean isApiAvailable() {
        return present && api != null;
    }
}