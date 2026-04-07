package com.asyncvault.api.service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight, thread-safe, generic service registry for platforms that do
 * not provide a built-in cross-plugin services manager (Fabric, BungeeCord/Waterfall).
 *
 * <p>Any plugin or mod can register arbitrary service instances keyed by interface
 * or class. Services are global to the JVM process, shared across all plugins on
 * the same platform instance. Not limited to AsyncVault services.
 *
 * <p>Example registration (provider plugin):
 * <pre>
 * AsyncVaultServiceRegistry.getInstance().register(EconomyProvider.class, myProvider);
 * </pre>
 *
 * <p>Example lookup (consumer plugin):
 * <pre>
 * Optional&lt;EconomyProvider&gt; economy =
 *     AsyncVaultServiceRegistry.getInstance().getProvider(EconomyProvider.class);
 * </pre>
 */
public final class AsyncVaultServiceRegistry {

    private static final AsyncVaultServiceRegistry INSTANCE = new AsyncVaultServiceRegistry();

    private final ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    private AsyncVaultServiceRegistry() {}

    /**
     * Returns the global singleton registry instance.
     */
    public static AsyncVaultServiceRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a service implementation. Replaces any previously registered
     * implementation for the same service class.
     *
     * @param serviceClass the service interface or class to register under
     * @param instance     the implementation instance
     * @param <T>          service type
     */
    public <T> void register(Class<T> serviceClass, T instance) {
        Objects.requireNonNull(serviceClass, "serviceClass");
        Objects.requireNonNull(instance, "instance");
        services.put(serviceClass, instance);
    }

    /**
     * Unregisters the service implementation for the given class, if any.
     *
     * @param serviceClass the service interface or class to remove
     */
    public <T> void unregister(Class<T> serviceClass) {
        Objects.requireNonNull(serviceClass, "serviceClass");
        services.remove(serviceClass);
    }

    /**
     * Returns the registered implementation for the given service class, or
     * {@link Optional#empty()} if none is registered.
     *
     * @param serviceClass the service interface or class to look up
     * @param <T>          service type
     * @return the registered provider, if present
     */
    public <T> Optional<T> getProvider(Class<T> serviceClass) {
        Objects.requireNonNull(serviceClass, "serviceClass");
        return Optional.ofNullable(serviceClass.cast(services.get(serviceClass)));
    }

    /**
     * Returns true if a provider is registered for the given service class.
     *
     * @param serviceClass the service interface or class to check
     */
    public boolean isRegistered(Class<?> serviceClass) {
        Objects.requireNonNull(serviceClass, "serviceClass");
        return services.containsKey(serviceClass);
    }

    /**
     * Removes all registered services. Intended for use during platform shutdown.
     */
    public void clear() {
        services.clear();
    }
}
