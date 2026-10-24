# Proteus

A shared UI toolkit for Olympus mods: animated widgets, themes and toasts.

Proteus is the UI library behind Olympus mods, and it also works on its own. The style, accent color and toast placement you pick apply to every mod that uses it, and it can draw the game's own toasts in the same style.

- **Modern style** with rounded widgets, background blur, smooth animations and an accent color (presets, a custom hue, or a chroma that cycles through the rainbow)
- **Classic style** that brings back the old Minecraft look: dirt background, bevelled gray buttons, no animation. It is drawn from GUI sprites under `proteus:classic`, so resource packs can restyle it
- **Toasts** drawn by Proteus: they ease in and out, show an item icon, grow to fit their text, appear over menus and in game, and can sit in any corner
- **One menu for every mod**: each mod built on Proteus gets a card at the top of its menu, so you can go from Orpheus to Mneme to the Proteus settings without closing anything. When there are more cards than fit, the arrows page through them
- **Game toasts** for advancements, recipes, tutorial hints and system notices can be drawn by Proteus. Each kind can be set back to the vanilla toast
- One toolkit across every supported version: widgets are drawn through a small `Canvas` interface, and each Minecraft range only implements that interface

Open the settings with the Proteus key (unbound by default, set it under Controls, Miscellaneous), with the config button in Mod Menu or the mods list, or from the Proteus card in any mod's menu. Settings are stored in `config/proteus/ui.json`.

## For mod authors

Add Proteus as a dependency (the artifact name follows the jar: `proteus-<loader>-<minecraft versions>`):

```groovy
repositories { mavenLocal() }   // until the library is on a public Maven repository

dependencies {
    modImplementation "com.github.olympus.proteus:proteus-fabric-1.21-1.21.1:1.0.0"   // Fabric
    implementation "com.github.olympus.proteus:proteus-neoforge-1.21-1.21.1:1.0.0"    // NeoForge
}
```

Then declare it as a required dependency in `fabric.mod.json`, `neoforge.mods.toml` or `mods.toml`.

A menu is a `UiApp`. Proteus owns the Minecraft screen and forwards drawing and input:

```java
public final class MyUi implements UiApp {
    private final Gui gui;

    public MyUi(UiHost host) {
        this.gui = new Gui(host);
    }
    // resize, tick, render(Canvas, mouseX, mouseY), mousePressed, keyPressed, ...
}

Screen screen = new ProteusScreen(Component.translatable("mymod.title"), parent, MyUi::new);
```

To give the menu a card in the hub instead, register it once when the client starts and open the hub on it:

```java
Hub.register("mymod", () -> Component.translatable("mymod.title").getString(), "compass", MyUi::new);

Screen screen = ProteusScreen.open(parent, "mymod");
```

The icon is an item id, and a name without a namespace means `minecraft:`. The Proteus card is always there and always last. `UiShell` already keeps its window below the card bar; a plain `UiApp` should start drawing below `host.topInset()`.

- `UiShell` is a ready-made base class for an animated menu window: extend it, implement `renderContent`, and you get the open and close animation, the backdrop, the theme and the input forwarding
- `Gui` is the immediate-mode toolkit: buttons, steppers, segmented controls, sliders, switches, single-line text fields, a multi-line text area with word wrap (`textArea`), scroll areas and tooltips, all themed. `focus` and `insertAtFocus` let a screen move the cursor or write into the field being edited
- `AppearancePanel` is the Appearance tab from the Proteus card, for a mod that wants the same settings inside its own screen
- `ToastOverlay.show(title, body, itemName)` shows a toast, for example `ToastOverlay.show("Now Playing", "Song - Artist", "music_disc_cat")`
- `Overlays.add(overlay)` draws something on the HUD every frame, such as pinned notes. An `Overlay` says whether it is `visible()` and draws itself through a `Canvas`
- `Icons.register("modid:name", classicWidth, classicHeight, painter)` adds a mod's own icon. The Modern style calls the painter, and the Classic style draws the sprite `modid:classic/icon/name`

## Supported versions

| Minecraft | Branch | Fabric | Forge | NeoForge |
|-----------|--------|:------:|:-----:|:--------:|
| 1.20-1.20.2 | [`1.20`](https://github.com/OlympusMods/Proteus/tree/1.20) | ✅ | ✅ | n/a |
| 1.20.3-1.20.4 | [`1.20.3`](https://github.com/OlympusMods/Proteus/tree/1.20.3) | ✅ | ✅ | ✅ |
| 1.20.5-1.20.6 | [`1.20.5`](https://github.com/OlympusMods/Proteus/tree/1.20.5) | ✅ | ✅ | ✅ |
| 1.21-1.21.1 | [`1.21`](https://github.com/OlympusMods/Proteus/tree/1.21) | ✅ | ✅ | ✅ |
| 1.21.2-1.21.3 | [`1.21.2`](https://github.com/OlympusMods/Proteus/tree/1.21.2) | ✅ | ✅ (1.21.3) | ✅ (1.21.3) |
| 1.21.4-1.21.5 | [`1.21.4`](https://github.com/OlympusMods/Proteus/tree/1.21.4) | ✅ | ✅ | ✅ |
| 1.21.6-1.21.8 | [`1.21.6`](https://github.com/OlympusMods/Proteus/tree/1.21.6) | ✅ | ✅ | ✅ |
| 1.21.9-1.21.10 | [`1.21.9`](https://github.com/OlympusMods/Proteus/tree/1.21.9) | ✅ | ✅ | ✅ |
| 1.21.11 | [`1.21.11`](https://github.com/OlympusMods/Proteus/tree/1.21.11) | ✅ | ✅ | ✅ |
| 26.1-26.1.2 | [`26.1`](https://github.com/OlympusMods/Proteus/tree/26.1) | ✅ | ✅ | ✅ |
| 26.2-26.3 | [`main`](https://github.com/OlympusMods/Proteus/tree/main) | ✅ | ✅ | ✅ |

Each range of Minecraft versions that one jar per loader can cover has its own branch, named after the first version of the range. `main` is always the newest one.

NeoForge has no stable release for 1.20.2 or 1.21.2 and Forge has none for 1.21.2.


## Installing

1. Download the jar for your loader and Minecraft version from [Modrinth](https://modrinth.com/mod/proteus) or [Releases](https://github.com/OlympusMods/Proteus/releases).
2. Put it in your `mods` folder. It works on its own, and mods that use Proteus tell you when it is needed. Fabric also needs Fabric API.

## Building

Each branch is one root Gradle build that drives a project per loader: `fabric`, `forge` and `neoforge`. They all compile the shared code in `common`. Every loader project is a Gradle build of its own, so it can also be opened and built on its own. Use the JDK each loader needs (`settings.gradle` lists them; Fabric needs Java 25 to run Gradle itself).

```sh
# one loader
./gradlew :fabric:build

# every loader on this branch
./gradlew build
```

Finished jars are collected into `build/dist` and named `proteus-<loader>-<minecraft versions>-<version>.jar`. The mod version is set in the root `gradle.properties`.

## Publishing

Releases go to Modrinth and CurseForge from the same build. Put `modrinthToken` and `curseforgeToken` in `~/.gradle/gradle.properties` (or set `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`), set the two project ids at the top of `publish.gradle`, and add a `## <version>` entry to `CHANGELOG.md`, which becomes the changelog of every upload.

```sh
# print what would be uploaded, send nothing
./gradlew publishMods -PdryRun=true

# one site, or both
./gradlew publishModrinth
./gradlew publishCurseforge
./gradlew publishMods

# one loader
./gradlew :neoforge:publishModrinth
```

Without any token the tasks only do a dry run. The Minecraft versions each jar is uploaded for come from `publish_minecraft_versions` in each loader's `gradle.properties`. Modrinth keeps the environment on each version, so `publishModrinth` finishes by setting the new version to client only; change that in `publish.gradle` if the mod also runs on servers.

## Project layout

This branch holds Minecraft 26.1 to 26.1.2. The other ranges are on the branches listed above.

```
common/src/main/java        Shared code
common/src/main/resources   Language file, textures and icon
fabric/  forge/  neoforge/  Thin per-loader projects
publish.gradle              Modrinth and CurseForge setup shared by every loader
tools/sync.sh               Copies a folder from this branch to every other branch
```

`common` holds the toolkit in `client/ui`, which has no version-specific code, and the glue for this Minecraft version (`GuiCanvas`, `ProteusScreen`, the toast and HUD hooks, mixins). Every branch keeps the same paths, so a fix is made once and cherry-picked to the other branches. A folder that is the same everywhere can also be copied over in one go, for example `tools/sync.sh common/src/main/java/com/github/olympus/proteus/client/ui`, which commits it to every other branch.

When a Minecraft update breaks the code, cut a new branch from the newest one, fix what the compiler reports, and narrow the version range on the branch it came from. A branch covers the range where one jar per loader still loads and its mixin targets still exist.

## Contributing

Bug reports and pull requests are welcome. For a bug, include your Minecraft version, loader and `latest.log`. Commit messages follow `type: short description` (`feat`, `fix`, `chore`, `refactor`).

## License

[GPL-3.0](LICENSE)
