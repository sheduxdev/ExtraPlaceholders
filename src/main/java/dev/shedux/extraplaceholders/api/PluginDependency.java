package dev.shedux.extraplaceholders.api;

/**
 * Interface for plugin dependency tracking
 *
 * @author sheduxdev
 * @since 1.0.0
 */
public interface PluginDependency {

    /**
     * Checks if the dependency is present and available
     *
     * @return true if dependency is available, false otherwise
     */
    default boolean isPresent() {
        return true;
    }
}