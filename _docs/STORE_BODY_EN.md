# Compass to Map

> Auto-register structures **and/or biomes** you find with Explorer's Compass / Nature's Compass as JourneyMap waypoints — fully automatic.

Found a structure or biome with Explorer's Compass or Nature's Compass, then had to **manually** punch the coordinates into JourneyMap? This addon does it for you, the instant you find it.

- ✨ **Auto-waypoint on discovery** — the moment the compass locates it, a waypoint appears on JourneyMap. No manual entry.
- 🧭 **Explorer's Compass + Nature's Compass** — structures, biomes, or both.
- 🎨 **Category color-coding** — villages, fortresses, dungeons, ocean monuments, etc. each get a colour; modded structures get a stable hash-generated colour so they stay distinguishable in big modpacks.
- 🛡️ **OP-only `/tp` suggestion** — operators get a clickable teleport suggestion in the chat notification; survival players just see the coordinates. Server-friendly.
- 💡 **Pure addon** — no items, no blocks, no textures. Just the automation.
- 🛟 **Safe without JourneyMap** — inner-class isolation means it never crashes if JM isn't installed (it simply falls back to chat).

## Supported loaders / versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | ✅ | ⚠️ chat-only |
| 1.20.1 | — | ✅ | ⚠️ chat-only |

- ✅ = full JourneyMap integration (auto waypoint registration)
- ⚠️ chat-only = JM integration disabled, chat notification fallback only (see Known limitations)
- — = NeoForge has no 1.20.1 release

## Configuration

`config/compasstomap-common.toml` (or the in-game Mod Config GUI on NeoForge/Forge):

| Key | Default | Description |
|---|---|---|
| `feature.enabled` | true | Master switch |
| `feature.enableStructure` | true | Explorer's Compass structure waypoints |
| `feature.enableBiome` | true | Nature's Compass biome waypoints |
| `notification.notifyOnFound` | true | Chat notification on discovery |
| `appearance.colorByCategory` | true | Category colours (off = single brand purple) |
| `appearance.persistentWaypoints` | true | Keep waypoints across restarts |

## Compatibility

| Mod | Support | Note |
|---|---|---|
| **Explorer's Compass** | optional | Structure-detection host |
| **Nature's Compass** | optional | Biome-detection host |
| **JourneyMap** | optional (CLIENT only) | Waypoint target; silently ignored if absent |
| Xaero's Minimap / Worldmap | not supported | Xaero has no public API |

At least one of Explorer's Compass / Nature's Compass is required for detection to do anything; with neither, the mod loads and stays idle.

## Known limitations

**Fabric builds — JourneyMap integration disabled.** On the 1.20.1 / 1.21.1 Fabric builds, automatic JM waypoint registration is currently disabled and only the chat notification fallback works. Reason: the JourneyMap Fabric jar requires Loom 1.14, which is unreleased; the current Loom can't link the JM API. Structure/biome discovery messages (with the OP `/tp` suggestion) still work normally. A reflection-bridge workaround is planned.

**No NeoForge 1.20.1 build** — NeoForge is derived from 1.21+. Use the Forge 1.20.1 build for 1.20.1.

## Install

1. Install your loader for your MC version (1.21.1 → NeoForge / Forge / Fabric · 1.20.1 → Forge / Fabric).
2. Install at least one of [Explorer's Compass](https://modrinth.com/mod/explorers-compass) / [Nature's Compass](https://modrinth.com/mod/natures-compass) (both is fine).
3. **Forge / NeoForge:** install [JourneyMap](https://modrinth.com/mod/journeymap) (recommended — without it there's no waypoint registration). **Fabric:** JM integration is currently disabled (chat-only).
4. **Fabric only:** also install [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port).
5. Drop the `compasstomap-2.0.3.jar` for your loader/MC (latest) from the releases page into `mods/`.

## License

MIT — modpack use, modification and redistribution OK, credit not required (welcome).

Author: KURONAMI · Built on Explorer's Compass / Nature's Compass / JourneyMap.
