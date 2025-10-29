package dev.shedux.extraplaceholders.tracker;

import dev.shedux.extraplaceholders.api.PluginDependency;
import lombok.Getter;
import xyz.refinedev.phoenix.CommonPlatform;
import xyz.refinedev.phoenix.Phoenix;
import xyz.refinedev.phoenix.PlatformGetter;

/**
 * Tracker for Phoenix plugin dependency
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Getter
public final class PhoenixTracker implements PluginDependency {

    private final boolean present;
    private final Phoenix api;
    private final CommonPlatform commonPlatform;

    /**
     * Initializes the Phoenix tracker and checks for API availability
     */
    public PhoenixTracker() {
        Phoenix tempApi = null;
        CommonPlatform tempCommonPlatform = null;

        try {
            tempCommonPlatform = PlatformGetter.getInstance();
        } catch (Exception | NoClassDefFoundError ignored) {
            // Phoenix platform not available
        }
        this.commonPlatform = tempCommonPlatform;

        try {
            tempApi = Phoenix.INSTANCE;
        } catch (Exception | NoClassDefFoundError ignored) {
            // Phoenix API not available
        }

        this.api = tempApi;
        this.present = tempApi != null;
    }
}