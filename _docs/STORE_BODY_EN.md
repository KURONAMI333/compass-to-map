# Compass to Map

Auto-registers the structures and biomes you find with Explorer's Compass and Nature's Compass as JourneyMap waypoints, the instant you find them.

Find a structure or biome with Explorer's Compass / Nature's Compass and you normally have to punch the coordinates into JourneyMap by hand. This addon does it for you — the moment the compass locates the target, a waypoint appears.

**Features**

- Auto-waypoints structures (Explorer's Compass) and biomes (Nature's Compass), or both
- Category colour-coding for villages, fortresses, monuments, etc.; modded structures get a stable hash colour so they stay distinguishable in big packs
- Operators get a clickable `/tp` suggestion in the discovery message; survival players just see the coordinates
- No items, blocks, or textures — just the automation, and it falls back to a chat message if JourneyMap isn't installed

**Config** (`config/compasstomap-common.toml`, or the in-game Mod Config GUI)

- `feature.enableStructure` / `feature.enableBiome` — toggle each compass
- `notification.notifyOnFound` — chat notification on discovery
- `appearance.colorByCategory` — category colours (off = single purple)
- `appearance.persistentWaypoints` — keep waypoints across restarts

**Dependencies**

- At least one of [Explorer's Compass](https://modrinth.com/mod/explorers-compass) or [Nature's Compass](https://modrinth.com/mod/natures-compass) — required for detection
- [JourneyMap](https://modrinth.com/mod/journeymap) (client) — the waypoint target; without it the mod falls back to chat
- Fabric only: [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port)

On the Fabric builds, JourneyMap waypoint registration is currently disabled and only the chat notification works (the JourneyMap Fabric jar needs an unreleased Loom version); discovery messages still work normally.

Install on the server and on each client — the server detects discoveries and tells the client, which registers the waypoint.

Free to use in any modpack. Source and issues: https://github.com/KURONAMI333/compass-to-map
