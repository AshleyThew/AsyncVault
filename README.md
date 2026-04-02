# AsyncVault

AsyncVault is a lightweight bridge API for Economy, Permission, and Chat services.

It does not provide economy or permission data by itself. Instead, provider plugins implement AsyncVault contracts and expose real backends (database, Redis, existing plugin data, etc.).

## Installation (Server Owners)

1. Download the AsyncVault build for your platform.
2. Put it in your server `plugins/` or `mods/` folder.
3. Install at least one provider plugin that supports AsyncVault.
4. Start the server.

After startup, other plugins can discover and use AsyncVault services through the registered providers.

## What AsyncVault Provides

- Stable API contracts for Economy, Permission, and Chat.
- CompletableFuture-backed result types via `AsyncResult<T>` and `SyncResult<T>`.
- AsyncResult uses `then(...)` / `thenSync(...)` for async and sync hops.
- SyncResult uses `then(...)` / `thenAsync(...)` for sync and async hops.
- Platform execution hooks so providers run work on the correct async/sync context.
- UUID-first API with legacy `playerName` overloads where needed.

## For Provider Developers

Use the API dependency:

```gradle
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.AshleyThew:AsyncVault:v1.0.0-alpha:asyncvault-api")
}
```

Implementation guide with full examples (including async database patterns):

- [API_PROVIDERS.md](API_PROVIDERS.md)

## For API Developers

How to consume Economy, Permission, and Chat providers from your plugin:

- [API_DEVELOPERS.md](API_DEVELOPERS.md)

## Build

```bash
./gradlew build
./gradlew bundleJars
```

Bundles are generated in `build/libs/`.
