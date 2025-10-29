package dev.shedux.extraplaceholders.api;

/**
 * Interface for plugin dependency tracking with consistent API
 * Provides standardized methods for checking plugin availability
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface PluginDependency {

    /**
     * Checks if the dependency is present and available
     *
     * @return true if dependency is available, false otherwise
     */
    boolean isPresent();

    /**
     * Gets the dependency name for logging purposes
     *
     * @return the dependency name
     */
    String getDependencyName();

    /**
     * Validates that the API is fully available and ready for use
     *
     * @return true if API can be safely used
     */
    boolean isApiAvailable();
}