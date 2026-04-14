# AsyncVault

[![Latest Release](https://img.shields.io/github/v/release/AshleyThew/AsyncVault?display_name=tag)](https://github.com/AshleyThew/AsyncVault/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/AshleyThew/AsyncVault/build.yml?branch=main&label=build)](https://github.com/AshleyThew/AsyncVault/actions/workflows/build.yml)
[![Tests](https://img.shields.io/github/actions/workflow/status/AshleyThew/AsyncVault/ci.yml?branch=main&label=tests)](https://github.com/AshleyThew/AsyncVault/actions/workflows/ci.yml)
[![Coverage](https://codecov.io/gh/AshleyThew/AsyncVault/branch/main/graph/badge.svg)](https://codecov.io/gh/AshleyThew/AsyncVault)

AsyncVault is a lightweight bridge API for Economy, Permission, and Chat services.

It does not provide economy or permission data by itself. Instead, provider plugins implement AsyncVault contracts and expose real backends (database, Redis, existing plugin data, etc.).

## Installation (Server Owners)

1. Download the AsyncVault build for your platform.
2. Put it in your server `plugins/` or `mods/` folder.
3. Install at least one provider plugin that supports AsyncVault.
4. Start the server.

After startup, other plugins can discover and use AsyncVault services through the registered providers.

## Supported Platforms

| Platform               | Module                  | Notes                                                               |
| ---------------------- | ----------------------- | ------------------------------------------------------------------- |
| Spigot / Paper / Folia | `asyncvault-spigot`     | Includes Folia regional scheduler and Paper async scheduler support |
| Fabric                 | `asyncvault-fabric`     | Fabric Loader 0.14+                                                 |
| Sponge                 | `asyncvault-sponge`     | SpongeAPI 10                                                        |
| Velocity               | `asyncvault-velocity`   | Velocity 3.x proxy                                                  |
| BungeeCord / Waterfall | `asyncvault-bungeecord` | BungeeCord + Waterfall-compatible proxy                             |

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
    compileOnly("com.github.AshleyThew:AsyncVault:{version}:asyncvault-api")
}
```

Implementation guide with full examples (including async database patterns):

- [API_PROVIDERS.md](API_PROVIDERS.md)

## For API Developers

How to consume Economy, Permission, and Chat providers from your plugin:

- [API_DEVELOPERS.md](API_DEVELOPERS.md)

## Build And Test

```bash
./gradlew build
./gradlew bundleJars
./gradlew :asyncvault-api:test :asyncvault-api:jacocoTestReport
```

Bundles are generated in `build/libs/`.

- Test report: `asyncvault-api/build/reports/tests/test/index.html`
- Coverage report: `asyncvault-api/build/reports/jacoco/test/html/index.html`
