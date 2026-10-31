# Tom's Simple Storage search cache fix

Client-side Forge mod for Minecraft 1.20.1 that removes the per-keystroke freeze when searching in
[Tom's Simple Storage](https://github.com/tom5454/Toms-Storage) terminals (tested against 1.7.1),
and the constant render-thread cost of EMI's craftables sidebar while a terminal is open.

## Search freeze

### The problem

A plain terminal search (no `@`, `#` or `$` prefix) first matches item names. For every stored item
whose name does not match, it then builds that item's full tooltip and matches against it. Building
a tooltip fires `ItemTooltipEvent`, so every tooltip mod in the pack (Apotheosis, Curios, ...) runs
for every item, on the render thread.

Tom's caches those tooltips in a Guava cache with `expireAfterAccess(5, SECONDS)`. After a short
pause, or as the query narrows and items that used to match by name stop matching, hundreds of
tooltips are rebuilt on a single keystroke. In a large modpack that is a visible freeze.

### The fix

A mixin runs after `AbstractStorageTerminalScreen`'s static initializer and wraps the three search
caches (`tooltipCache`, `nbtCache`, `tagCache`) in caches with a longer lifetime whose loader is the
stock cache. Tom's own code still computes every value, so search results are identical; only the
cache lifetime changes. Cache keys include the item's NBT, so a changed item gets a fresh tooltip.

The first search of a session still builds each item's tooltip once.

## EMI craftables in a terminal

### The problem

While EMI's craftables sidebar is visible, `EmiScreenManager.updateCraftables` rebuilds an
`EmiPlayerInventory` and compares it with the previous one every 50 ms (400 ms otherwise). Inside a
storage terminal, Tom's EMI integration makes that inventory the whole storage network, and hashing
and comparing every stack goes through JEMI's JEI subtype lookup. That is 20 full snapshots a second,
while Tom's only refreshes the terminal's item list about every 500 ms.

### The fix

An optional mixin (skipped when EMI is absent) raises that 50 ms interval to 500 ms, only while the
open screen is a storage terminal. The craftables list is computed exactly as before; it can only
appear up to the interval later. Every other screen keeps EMI's own timing.

## Config

`config/tomsstoragesearchcache-client.toml`

| Option | Default | Meaning |
| --- | --- | --- |
| `search.cacheSeconds` | `3600` | How long an unused cached entry is kept. `5` restores stock behaviour. |
| `emi.terminalSyncMillis` | `500` | EMI craftables refresh interval inside a terminal. `50` restores stock behaviour. |

## Building

No Gradle: `build.sh` compiles against the libraries of an existing Forge 1.20.1-47.4.13 install and
the Tom's Simple Storage and EMI jars from a mods folder.

```sh
LIBS="$APPDATA/PrismLauncher/libraries" MODS="path/to/minecraft/mods" ./build.sh
```

Output: `build/TomsStorageSearchCache-1.20.1-1.1.0.jar`. Requires JDK 17+.

## License

Apache-2.0
