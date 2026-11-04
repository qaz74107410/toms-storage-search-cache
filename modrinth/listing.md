# Modrinth listing — Tom's Storage Search Cache Fix

Copy-paste source for the Modrinth project page. Metadata block at the bottom is what the API needs.

---

## Title

`Tom's Storage Search Cache Fix`

## Summary (≤ 256 chars)

> Stops the per-keystroke freeze when searching Tom's Simple Storage terminals. A client-side mixin that lengthens the tooltip cache instead of changing search results — plus it skips EMI's redundant 20-per-second inventory rebuilds inside a terminal.

## Categories

`optimization` · `storage` · `utility` — client-side only, Forge

## Body (Markdown)

```markdown
A client-side Forge mod for **Minecraft 1.20.1** that removes two render-thread bottlenecks in
[Tom's Simple Storage](https://modrinth.com/mod/toms-storage) terminals. Tested against Tom's 1.7.1.

Typing in a terminal search box should not cost frames. In a large modpack it does, twice.

## The search freeze

A plain terminal search (no `@`, `#` or `$` prefix) matches item names first. For every stored item
whose name does **not** match, it then builds that item's full tooltip and matches against it.
Building a tooltip fires `ItemTooltipEvent`, so every tooltip mod in the pack (Apotheosis, Curios, …)
runs — on the render thread.

Tom's caches those tooltips with Guava's `expireAfterAccess(5, SECONDS)`. After a short pause, or as
the query narrows and items that used to match by name stop matching, hundreds of tooltips are rebuilt
on a single keystroke. In a 365-mod pack that is a visible freeze on every character typed.

**What this mod does:** a mixin runs after `AbstractStorageTerminalScreen`'s static initializer and wraps
the three search caches (`tooltipCache`, `nbtCache`, `tagCache`) in caches with a longer lifetime whose
loader *is* the stock cache. Tom's own code still computes every value, and the cache keys still include
the item's NBT, so a changed item gets a fresh tooltip and **search results are identical** — only the
cache lifetime changes.

The first search of a session still builds each item's tooltip once.

## EMI inside a terminal

While EMI's craftables sidebar is visible, `EmiScreenManager.updateCraftables` rebuilds an
`EmiPlayerInventory` and compares it with the previous one every 50 ms (400 ms otherwise). Inside a
storage terminal, Tom's EMI integration makes that inventory the whole storage network, and hashing and
comparing every stack goes through JEMI's JEI subtype lookup — 20 full snapshots a second, while Tom's
only refreshes the terminal's item list about every 500 ms. Profiling measured **80–100 ms of render
thread per snapshot** in a large network.

**What this mod does** (optional mixins, skipped entirely when EMI is absent and active only while a
storage terminal is open):

- **Skip unchanged snapshots** — the snapshot is only rebuilt after the terminal receives an item list
  update from the server, or a menu slot changed (tracked by a packet counter and a stack
  identity/count fingerprint). Otherwise it would be byte-identical.
- **Re-pace the interval** — 50 ms becomes 500 ms, matching how often Tom's refreshes the list.

The craftables list is computed exactly as before, and every other screen keeps EMI's own behaviour.

## Measured

Java Flight Recorder, ~365-mod pack, render thread, ~4 seconds of continuous typing:

| | Before | After |
|---|---:|---:|
| Tom's search samples | 175 | **43** |
| of which tooltip building | 167 | **10** |

⚠️ Honest caveat: the search fix is tested in game. **EMI snapshot skipping has not yet been tested in
game** — it is correct by construction and disabled by config, but untested.

## Requirements

- Minecraft **1.20.1**, Forge 47.x (client-side only — do not install on a server)
- [Tom's Simple Storage](https://modrinth.com/mod/toms-storage) 1.7.x
- Optional: [EMI](https://modrinth.com/mod/emi) — only the terminal fix applies if present
- Optional: [Cloth Config](https://modrinth.com/mod/cloth-config) for the in-game config screen

## Config

In game: **Mods → Tom's Simple Storage search cache fix → Config**, or edit
`config/tomsstoragesearchcache-client.toml`:

| Option | Default | Meaning |
|---|---:|---|
| `search.cacheSeconds` | `3600` | How long an unused cached entry is kept. `5` restores stock behaviour. |
| `emi.terminalSyncMillis` | `500` | EMI craftables refresh interval inside a terminal. `50` restores stock behaviour. |
| `emi.skipUnchangedSnapshots` | `true` | Skip EMI's snapshot in a terminal while nothing changed. `false` restores stock behaviour. |

Cache lifetime changes need a game restart; the EMI interval applies immediately.

## Notes

- Client-side only. Nothing about the server, the network protocol or item handling changes.
- Every behaviour can be reverted to stock with the config values above — the mod is a cache-lifetime
  and scheduling change, not a logic change.
- Built without Gradle: `build.sh` compiles against the libraries of an existing Forge 1.20.1-47.4.13
  install plus the Tom's, EMI and Cloth Config jars.

## Source & license

Source, issues and build script: <https://github.com/qaz74107410/toms-storage-search-cache>
Licensed **Apache-2.0**. Tom's Simple Storage is MIT and by tom5454 — this mod ships none of its code.
```

---

## Metadata for the API / web form

| Field | Value |
|---|---|
| Project type | `mod` |
| Slug | `toms-storage-search-cache` |
| Client side | `required` |
| Server side | `unsupported` |
| Loaders | `forge` |
| Game versions | `1.20.1` (add `1.20.2`–`1.20.6` only after testing) |
| License | `Apache-2.0` |
| Categories | `optimization`, `storage`, `utility` |
| Icon | `icon.png` (512×512) |
| Source URL | https://github.com/qaz74107410/toms-storage-search-cache |
| Issues URL | https://github.com/qaz74107410/toms-storage-search-cache/issues |
| Environment | Client only |
| Dependency (version) | `XZNI4Cpy` — Tom's Simple Storage, `required` |
| Optional dependency | EMI (`fRiHVvU7`), Cloth Config API (`9s6osm5g`) |
| Version number | `1.1.0+1.20.1` |
| Release channel | `release` |
| Featured | no (leave default) |

Base mod reference, verified 2026-11-04: `toms-storage` — 21,667,352 downloads, 1,364 followers,
supports `forge` on 1.20.1, latest 1.20.1 Forge version `1.20-1.7.1`.
