package dev.shedux.extraplaceholders.core;

import dev.shedux.extraplaceholders.tracker.BoltTracker;
import dev.shedux.extraplaceholders.tracker.PhoenixTracker;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Initializes and manages plugin trackers
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
     */
    public void initialize() {
        bolt = new BoltTracker();
        phoenix = new PhoenixTracker();
    }
}