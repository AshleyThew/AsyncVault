# AsyncVault Examples

Practical examples showing how to expose AsyncVault providers from common plugins and plugin data sources.

## Available Examples

- [Full Spigot Provider Plugin Project](spigot-provider-plugin)
- [EssentialsX Economy](essentialsx.md)
- [LuckPerms Permission + Chat](luckperms.md)
- [PlaceholderAPI Chat Provider](placeholderapi.md)

## Notes

These examples use real plugin APIs and service lookups rather than mock bridge abstractions. They also show the current AsyncResult/SyncResult chaining model, where async chains use `then(...)` and sync handoffs use `thenSync(...)`. Economy examples now include capability checks for world-scoped, bank-scoped, and multi-currency providers (`supportsWorldScoping`, `supportsBankAccounts`, `supportsMultipleCurrencies`) and how to resolve scoped providers. The full Spigot example keeps optional plugin integrations in separate registrar classes so the main plugin entrypoint can load without Essentials, LuckPerms, or PlaceholderAPI present. If you want to consume providers from another plugin, see the API developer guide in [API_DEVELOPERS.md](../../API_DEVELOPERS.md).
