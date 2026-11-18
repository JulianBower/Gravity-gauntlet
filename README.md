# Earth's Gravity Gauntlet (`earths_gravity_gauntlet`)

Minecraft **1.21.1** / **NeoForge 21.1.x** / Java 21.

## Build
```
gradle build          # or: gradle wrapper && ./gradlew build
```
The mod jar lands in `build/libs/`. Use `gradle runClient` to test. Adjust `neo_version` in `gradle.properties` if needed.

## Obtain
1. Mine **Gravity Core Ore** (Y -63..-58, just above bedrock; Diamond/Netherite pickaxe) -> drops **Earth's Gravity Core**.
2. Craft the **Earth's Gravity Gauntlet** (3x3): core in the centre, 4 Netherite Ingots on the edges,
   4 Reinforced Deepslate / Crying Obsidian in the corners.

## Controls
| Input | Effect |
|---|---|
| Hold Right-Click | Live telekinetic gravity point at `cursorDistance` (never pulls you) |
| Scroll (gauntlet in main hand) | Distance +-1.5 blocks per notch (2.0 - 24.0) |
| Hold Right-Click + Left-Click | Plant anchor on the targeted block face (up to 24 blocks) |
| Sneak + Hold Right-Click + Left-Click | Plant anchor in mid-air at the scrolled distance |
| Sneak + Right-Click (tap) on mob | Toggle mark (max 8); tap on air clears all |
| `G` | Toggle Self-Pull Mode |

Anchors last 8 s, max 2 per player (a third replaces the oldest). Two anchors within 14 blocks tear mobs
between them (4.0 armor-bypassing damage every 10 ticks). In Self-Pull Mode planted anchors pull and hold *you*
(no fall damage while active + 3 s after).

## Notes
- Server `allow-flight=true` may be needed on dedicated servers, since holding a player mid-air can trip the vanilla fly-kick check.
- Code was written without a compiler against the Minecraft jars; only a syntax check was run. If your NeoForge
  build differs slightly in a mapping, fix the named line from the compile error.
