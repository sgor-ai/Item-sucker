# ItemSucker

<p align="center">
  <img src="src/main/resources/icon.png" alt="ItemSucker icon" width="128" height="128">
</p>

<p align="center">
  <strong>Automatic dropped-item collection for Fabric 26.2</strong><br>
  Select what to collect, then let ItemSucker move to matching drops.
</p>

<p align="center">
  <img src="docs/images/itemsucker-overview.svg" alt="ItemSucker overview" width="900">
</p>

<p align="center">
  <a href="https://www.minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-26.2-4b7bec?logo=minecraft&logoColor=white" alt="Minecraft 26.2"></a>
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/Fabric-client-20232a?logo=fabric" alt="Fabric client mod"></a>
  <a href="https://adoptium.net/"><img src="https://img.shields.io/badge/Java-25-e76f00?logo=openjdk&logoColor=white" alt="Java 25"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-2f80ed.svg" alt="MIT license"></a>
</p>

## What ItemSucker does

ItemSucker is a **client-side** Fabric mod. It searches for dropped item entities within a fixed **64-block radius**, applies your whitelist or blacklist, and moves close enough to collect the items.

The collection device shown in the icon represents the complete loop:

1. **Detect** dropped items around the player.
2. **Filter** them using the selected item rules.
3. **Move** using Teleport mode or optional Baritone pathing.
4. **Collect** the items and optionally return to the starting position.

<p align="center">
  <img src="docs/images/itemsucker-flow.svg" alt="ItemSucker detection, filtering, movement and collection flow" width="900">
</p>

## Demo

The GIF below contains the complete supplied screen recording from start to finish. It shows ItemSucker enabled, matching drops being selected, movement to the items, and the collection result.

![Complete ItemSucker demonstration](docs/images/itemsucker-demo.gif)

## Features

- Fixed 64-block search radius.
- **Whitelist** mode: collect only selected items.
- **Blacklist** mode: collect everything except selected items.
- **Teleport** mode: instant movement to each matching drop.
- **Baritone** mode: optional path-based movement that walks to matching drops.
- Stable green/red target highlighting in the item-selection screen.
- Searchable, scrollable item grid.
- Optional return to the starting position after a collection session.
- Optional end-of-session pickup summary.
- Mod Menu configuration screen when Mod Menu is installed.
- Keybinds for opening the GUI and toggling the mod.
- Settings saved in `config/itemsucker.json`.

## Requirements

### Required

| Component | Version |
| --- | --- |
| Minecraft Java Edition | `26.2` |
| Fabric Loader | `0.19.3` or newer |
| Fabric API | A build matching Minecraft `26.2` |
| Java | `25` |
| ItemSucker | `itemsucker-26.2-1.0.0.jar` |

### Optional

| Component | Needed for |
| --- | --- |
| Baritone Meteor | `/itemsucker move baritone` |
| Mod Menu | Opening ItemSucker settings from the Mods menu |

ItemSucker starts without Baritone or Mod Menu. If Baritone is unavailable, Baritone mode cannot be selected and Teleport remains available.

## Installation

1. Install Fabric Loader for Minecraft `26.2`.
2. Install Fabric API for the same Minecraft version.
3. Open the Minecraft `mods` folder:
   - **Windows:** press `Win + R`, enter `%appdata%\.minecraft\mods`, then press Enter.
   - **Linux:** `~/.minecraft/mods`
   - **macOS:** `~/Library/Application Support/minecraft/mods`
4. Copy `build/libs/itemsucker-26.2-1.0.0.jar` into that folder, or use the JAR from a release.
5. For Baritone movement, copy a compatible `baritone-meteor` JAR into the same folder.
6. For Mod Menu integration, copy a compatible Mod Menu JAR into the same folder.
7. Launch the Fabric profile for Minecraft `26.2`.

Do **not** copy the source folder into `mods`; Minecraft loads the compiled JAR.

## First launch

1. Enter a world where automated movement is allowed.
2. Run `/itemsucker gui`, or press **M**, to open the item selector.
3. Choose **Whitelist** to collect only selected items, or **Blacklist** to exclude selected items.
4. Search for an item and click it to select or deselect it.
5. Press **Done** to save.
6. Choose a movement mode:

   ```text
   /itemsucker move teleport
   ```

   or, with Baritone installed:

   ```text
   /itemsucker move baritone
   ```

7. Enable the module:

   ```text
   /itemsucker on
   ```

The collection radius is intentionally fixed at 64 blocks. Ground, pickup-delay and teleport-collision checks remain internal safety checks and are not user commands.

## Commands

All commands are client-side:

```text
/itemsucker                    Toggle the module
/itemsucker on                 Enable collection
/itemsucker off                Disable collection
/itemsucker toggle             Toggle collection
/itemsucker gui                Open the item selector
/itemsucker mode whitelist     Collect only selected items
/itemsucker mode blacklist     Exclude selected items
/itemsucker move teleport      Use instant movement
/itemsucker move baritone      Use Baritone pathing
/itemsucker return on          Return after the session
/itemsucker return off         Stay at the last collected item
/itemsucker return toggle      Toggle return-to-origin
/itemsucker notify on          Show collection summaries
/itemsucker notify off         Hide collection summaries
```

The old `ground`, `pickupable`, `collision-check` and configurable `range` commands are intentionally not part of the current interface.

## Movement modes

<p align="center">
  <img src="docs/images/itemsucker-modes.svg" alt="Comparison of ItemSucker Teleport and Baritone modes" width="900">
</p>

### Teleport

Teleport mode places the local player at each matching item. The same target is not repeatedly teleported to, which keeps movement stable. Collision checks remain enabled.

### Baritone

Baritone mode sends matching item targets to Baritone and lets it path to them. Goals are refreshed only when targets change or when a periodic refresh is needed. When return-to-origin is enabled, Baritone is asked to return after the collection session.

Both modes automate movement. Always check the rules of the world or server before using them.

## Keybinds and Mod Menu

- **M:** open the ItemSucker item-selection screen.
- **Toggle key:** unassigned by default; assign it under **Options → Controls**.
- **Mod Menu:** if installed, open Minecraft's Mods menu and select ItemSucker.

## Configuration

The file below is created automatically:

```text
.minecraft/config/itemsucker.json
```

The GUI and commands control the supported settings. The collection radius is always 64 blocks; the internal safety checks are deliberately kept enabled.

## Build from source

This repository includes the Gradle wrapper:

```powershell
.\gradlew.bat clean build
```

The output JAR is:

```text
build/libs/itemsucker-26.2-1.0.0.jar
```

To launch a development client with the local Baritone and Mod Menu dependencies:

```powershell
.\gradlew.bat runClient
```

The `run/` directory and build output are ignored by Git.

## Project layout

```text
src/main/java/com/itemsucker/  Core logic, commands, GUI and integrations
src/main/resources/             Fabric metadata, icon and translations
libs/                           Local development dependencies
docs/images/                    README illustrations and complete demo GIF
```

## Safety and server policy

Teleport mode changes the local player position instantly. Baritone mode automates pathing. Either mode may be disallowed by multiplayer server rules or trigger anti-cheat systems. Use ItemSucker only in single-player or where automation is explicitly permitted. The authors are not responsible for kicks, bans, lost items or other server-side consequences.

## License

ItemSucker is released under the [MIT License](LICENSE). It is not affiliated with Mojang, Microsoft, Fabric, Meteor or Baritone.
