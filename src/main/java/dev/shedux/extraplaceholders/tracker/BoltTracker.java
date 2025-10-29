package dev.shedux.extraplaceholders.tracker;

import dev.shedux.extraplaceholders.api.PluginDependency;
import lombok.Getter;
import xyz.refinedev.practice.api.BoltAPI;

/**
 * Tracker for Bolt plugin dependency
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Getter
public final class BoltTracker implements PluginDependency {

    private final boolean present;
    private final BoltAPI api;

    /**
     * Initializes the Bolt tracker and checks for API availability
     */
    public BoltTracker() {
        BoltAPI tempApi = null;

        try {
            tempApi = BoltAPI.INSTANCE;
        } catch (Exception | NoClassDefFoundError ignored) {
            // Bolt API not available
        }

        this.api = tempApi;
        this.present = true;
    }
}