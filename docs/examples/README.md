# AsyncVault Examples

Practical examples showing how to expose AsyncVault providers from common plugins and plugin data sources.

## Available Examples

- [Full Spigot Provider Plugin Project](spigot-provider-plugin)
- [EssentialsX Economy](essentialsx.md)
- [LuckPerms Permission + Chat](luckperms.md)
- [PlaceholderAPI Chat Provider](placeholderapi.md)

## Notes

These examples use real plugin APIs and service lookups rather than mock bridge abstractions. They also show the current AsyncResult/SyncResult chaining model, where async chains use `then(...)` and sync handoffs use `thenSync(...)`. Economy examples now include capability checks for world-scoped, bank-scoped, and multi-currency providers (`supportsWorldScoping`, `supportsBankAccounts`, `supportsMultipleCurrencies`) and how to resolve scoped providers. The full Spigot example keeps optional plugin integrations in separate registrar classes so the main plugin entrypoint can load without Essentials, LuckPerms, or PlaceholderAPI present. If you want to consume providers from another plugin, see the API developer guide in [API_DEVELOPERS.md](../../API_DEVELOPERS.md).

## Proxy Platforms (Velocity and BungeeCord)

Velocity and BungeeCord are proxy servers and do not have a "main server thread" in the same sense as Spigot/Fabric/Sponge. The `syncExecutor()` on these platforms routes work through the proxy's managed scheduler rather than a dedicated main thread. The `asyncExecutor()` uses the common ForkJoinPool for background I/O. Neither Velocity nor BungeeCord provides a built-in cross-plugin services manager, so providers on these platforms expose instances via a static plugin-owned registry (see [API_PROVIDERS.md](../../API_PROVIDERS.md) for patterns).
