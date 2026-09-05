Turn structures and biomes found with Explorer's Compass or Nature's Compass into JourneyMap waypoints without copying coordinates by hand.

When either compass finishes a search, Compass to Map creates the waypoint immediately. It can handle structures, biomes, or both, depending on which compass mods are installed.

## Features

- **Automatic waypoints** — records each successful compass result in JourneyMap.
- **Useful colours** — uses category colours for familiar structures and stable generated colours for modded structures.
- **Safer discovery messages** — shows coordinates to everyone and gives operators a clickable `/tp` suggestion.
- **Chat fallback** — still reports the discovery when JourneyMap integration is unavailable.

The config can toggle structure detection, biome detection, discovery messages, category colours, and persistent waypoints.

## Dependencies and limitations

At least one of [Explorer's Compass](https://modrinth.com/mod/explorers-compass) or [Nature's Compass](https://modrinth.com/mod/natures-compass) is required. [JourneyMap](https://modrinth.com/mod/journeymap) is required to create waypoints; without it, discoveries are reported in chat only.

JourneyMap integration is available on the Forge and NeoForge builds and on Fabric 1.21.4 or newer. Fabric 1.20.1 and 1.21.1 use the chat fallback. Fabric builds also require [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port).

All Rights Reserved. Modpack inclusion is allowed without permission or credit.

Bugs and questions: comment on the CurseForge page, or DM @kuronami333 on X.

[Source](https://github.com/KURONAMI333/compass-to-map)
