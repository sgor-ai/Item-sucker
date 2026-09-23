# ItemSucker

![ItemSucker overview](docs/images/itemsucker-overview.svg)

> A focused, standalone Fabric client mod that collects nearby dropped items with configurable filters and movement modes.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62b47a?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-client-2d2d2d?logo=fabric)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## See it in action

This is the complete screen recording from beginning to end. It shows the mod running in Minecraft, finding dropped items and reporting the collected items.

![Complete ItemSucker demo](docs/images/itemsucker-demo.gif)

The GIF is generated from the full `11.37-second` source video. It is resized only to keep the GitHub repository practical; no part of the recording is intentionally removed.

## What it does

ItemSucker is a **client-side** Fabric mod. It watches for dropped items around your player and moves close enough to collect them. You choose which items are allowed, then choose either instant Teleport movement or optional Baritone pathing.

If you only want the simple version: install Fabric, Fabric API and the ItemSucker JAR, then run `/itemsucker on`. Baritone is needed only when you explicitly choose `/itemsucker move baritone`.

![Collection flow](docs/images/itemsucker-flow.svg)

### Features

- Constant 64-block collection radius.
- Whitelist and blacklist item filters.
- Teleport mode with collision checks and optional return-to-origin.
- Optional Baritone mode for path-based movement.
- In-game item grid with search, scrolling and selection.
- ModMenu configuration screen.
- Keybinds for toggling the mod and opening the GUI.
- Optional end-of-session pickup summary.
- Settings persisted in `config/itemsucker.json`.

## Requirements — read this first

| Component | Required | Version |
| --- | --- | --- |
| Minecraft Java Edition | Yes | `26.2` |
| Fabric Loader | Yes | `>= 0.19.0` |
| Fabric API | Yes | matching `26.2` build |
| Java | Yes | `25` |
| Baritone Meteor | Optional | `26.2-SNAPSHOT` |
| Mod Menu | Optional | `20.0.1` or compatible |

### What is required

You must have:

1. Minecraft Java Edition `26.2`.
2. Fabric Loader `0.19.3` or newer.
3. Fabric API for Minecraft `26.2`.
4. Java `25`.
5. The ItemSucker JAR built from this project or downloaded from a release.

You need Baritone only for **Baritone movement mode**. You need Mod Menu only if you want to open the configuration screen from the Mods menu. ItemSucker still starts without either optional mod.

## Installation — step by step

1. Install Fabric Loader for Minecraft `26.2`.
2. Open your Minecraft mods folder:
   - Windows: press `Win + R`, enter `%appdata%\.minecraft\mods`, and press Enter.
   - Linux: open `~/.minecraft/mods`.
   - macOS: open `~/Library/Application Support/minecraft/mods`.
3. Put a Fabric API JAR matching Minecraft `26.2` in that folder.
4. Build this project with `.\gradlew.bat clean build`.
5. Copy `build/libs/itemsucker-26.2-1.0.0.jar` into the mods folder.
6. If you want Baritone mode, also put `baritone-meteor-26_2.jar` in the same folder.
7. If you want the Mod Menu screen, install a compatible Mod Menu JAR.
8. Start Minecraft using the Fabric profile.

Do not put the source folder itself in `.minecraft/mods`; Minecraft needs the compiled JAR.

### First launch

1. Enter a world where item automation is allowed.
2. Type `/itemsucker on`.
3. Open `/itemsucker gui` or press **M**.
4. Choose `Blacklist` to collect everything except selected items, or `Whitelist` to collect only selected items.
5. Select the items in the grid and press **Done**.
6. Choose `/itemsucker move teleport` for instant movement or `/itemsucker move baritone` for pathing.

The repository includes local Baritone and Mod Menu JARs only to make the development build reproducible. They are optional at runtime according to `fabric.mod.json`; use versions you are licensed and permitted to redistribute.

## Usage

### Keybinds

- **Unknown / unassigned**: toggle ItemSucker.
- **M**: open the item selection screen. Both bindings can be changed under **Options → Controls**.

### Commands

All commands are client-side and require no server permission:

```text
/itemsucker
/itemsucker on
/itemsucker off
/itemsucker toggle
/itemsucker gui
/itemsucker mode whitelist
/itemsucker mode blacklist
/itemsucker move teleport
/itemsucker move baritone
/itemsucker return on|off|toggle
/itemsucker notify on|off
```

### GUI

Open `/itemsucker gui`, press **M**, or use Mod Menu. Select items in the grid, search by display name or registry path, switch between whitelist/blacklist, choose movement mode, toggle return and notifications, and save with **Done**. The collection radius is fixed at 64 blocks; pickup and teleport safety checks remain enabled internally.

### Configuration explained

- **Blacklist**: collect every item except the items you select.
- **Whitelist**: collect only the items you select.
- **Teleport**: instantly move to matching items. Collision checks remain enabled.
- **Baritone**: ask Baritone to walk/path to matching items.
- **Return**: return to the starting position after the current collection session.
- **Notifications**: show a summary when a collection session finishes.
- **Collection radius**: fixed at 64 blocks. It is intentionally not configurable.

## Building from source

The project includes the Gradle wrapper and local compile-time dependencies:

```powershell
.\gradlew.bat clean build
```

The output is:

```text
build/libs/itemsucker-26.2-1.0.0.jar
```

Run a development client with Baritone and Mod Menu:

```powershell
.\gradlew.bat runClient
```

The development run directory is intentionally ignored by Git.

## Safety and server policy

Teleport mode changes the local player position instantly and may trigger anti-cheat systems. Baritone mode uses pathing but is still automated movement. Use ItemSucker only in single-player or on servers where automation is explicitly allowed. The authors are not responsible for kicks, bans, lost items, or other server-side consequences.

## Project layout

```text
src/main/java/com/itemsucker/       Java source
src/main/resources/                 Fabric metadata and translations
libs/                               local build dependencies
docs/images/                        README artwork
```

## License and attribution

ItemSucker is released under the MIT License. The standalone implementation is based on the ItemSucker concept extracted from Meteorist and is not affiliated with Mojang, Fabric, Meteor, or Baritone.
