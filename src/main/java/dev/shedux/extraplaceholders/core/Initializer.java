package dev.shedux.extraplaceholders.core;

import dev.shedux.extraplaceholders.tracker.BoltTracker;
import dev.shedux.extraplaceholders.tracker.PhoenixTracker;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Initializes and manages plugin dependency trackers
 * Provides centralized access to all tracker instances
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@UtilityClass
public class Initializer {

    @Getter
    private static BoltTracker bolt;

    @Getter
    private static PhoenixTracker phoenix;

    /**
     * Initializes all dependency trackers
     * Should be called once during plugin startup
     */
    public void initialize() {
        bolt = new BoltTracker();
        phoenix = new PhoenixTracker();
    }
}