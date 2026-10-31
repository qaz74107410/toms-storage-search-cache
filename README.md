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

Profiling showed each snapshot costs 80-100 ms of render thread in a large network, so spacing them
out is not enough. Optional mixins (skipped when EMI is absent), active only while the open screen is
a storage terminal:

- **Skip unchanged snapshots**: the snapshot is only rebuilt after the terminal received an item list
  update from the server, or a menu slot (inventory, crafting grid) changed - tracked by a packet
  counter and a stack identity/count fingerprint of the menu slots. Otherwise it would be identical.
- **Interval**: the 50 ms interval is raised to 500 ms, matching how often Tom's refreshes the list.

The craftables list is computed exactly as before. Every other screen keeps EMI's own behaviour.

## Test results

Measured with Java Flight Recorder in a ~365-mod pack (Cisco's Fantasy Medieval RPG), render thread:

| | Before | After |
| --- | --- | --- |
| Tom's search while typing (samples, ~4 s of typing) | 175 | 43 |
| of which tooltip building | 167 | 10 |

Search fix: tested in game, typing is smooth. EMI snapshot skipping: **not yet tested in game**.

## Config

In game: **Mods -> Tom's Simple Storage search cache fix -> Config** (needs
[Cloth Config](https://modrinth.com/mod/cloth-config), which most packs already have). The EMI
interval applies immediately; the cache lifetime needs a restart.

Or edit `config/tomsstoragesearchcache-client.toml`:

| Option | Default | Meaning |
| --- | --- | --- |
| `search.cacheSeconds` | `3600` | How long an unused cached entry is kept. `5` restores stock behaviour. |
| `emi.terminalSyncMillis` | `500` | EMI craftables refresh interval inside a terminal. `50` restores stock behaviour. |
| `emi.skipUnchangedSnapshots` | `true` | Skip EMI's snapshot in a terminal while nothing changed. `false` restores stock behaviour. |

## Building

No Gradle: `build.sh` compiles against the libraries of an existing Forge 1.20.1-47.4.13 install and
the Tom's Simple Storage, EMI and Cloth Config jars from a mods folder.

```sh
LIBS="$APPDATA/PrismLauncher/libraries" MODS="path/to/minecraft/mods" ./build.sh
```

Output: `build/TomsStorageSearchCache-1.20.1-1.1.0.jar`. Requires JDK 17+.

## License

Apache-2.0
