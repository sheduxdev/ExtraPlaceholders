package dev.shedux.extraplaceholders.tracker;

import dev.shedux.extraplaceholders.api.PluginDependency;
import lombok.Getter;
import xyz.refinedev.practice.api.BoltAPI;

/**
 * Tracker for Bolt plugin dependency
 * Manages Bolt API availability and access with safe initialization
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@Getter
public final class BoltTracker implements PluginDependency {

    private static final String DEPENDENCY_NAME = "Bolt";

    private final boolean present;
    private final BoltAPI api;

    /**
     * Initializes the Bolt tracker with safe API access
     * Handles missing dependencies gracefully without throwing exceptions
     */
    public BoltTracker() {
        BoltAPI tempApi = null;
        boolean isPresent = false;

        try {
            tempApi = BoltAPI.INSTANCE;
            isPresent = (tempApi != null);
        } catch (NoClassDefFoundError | Exception ignored) {
            // Bolt API not available - fail silently
        }

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