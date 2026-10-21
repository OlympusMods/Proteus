# Proteus

A shared UI toolkit for Olympus mods: animated widgets, themes and toasts.

Proteus is the UI library behind Olympus mods. On its own it only adds the shared look: players install it because a mod asks for it, and the style, accent color and toast placement they pick apply to every mod that uses it.

- **Modern style** with rounded widgets, background blur, smooth animations and an accent color (presets, a custom hue, or a chroma that cycles through the rainbow)
- **Classic style** that brings back the old Minecraft look: dirt background, bevelled gray buttons, no animation. It is drawn from GUI sprites under `proteus:classic`, so resource packs can restyle it
- **Toasts** drawn by Proteus: they ease in and out, show an item icon, grow to fit their text, appear over menus and in game, and can sit in any corner
- One toolkit across every supported version: widgets are drawn through a small `Canvas` interface, and each Minecraft range only implements that interface

Settings are stored in `config/proteus/ui.json`.

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

- `Gui` is the immediate-mode toolkit: buttons, steppers, segmented controls, sliders, switches, text fields, scroll areas and tooltips, all themed
- `AppearancePanel` is the ready-made Appearance tab, so a mod does not have to rebuild the style and toast settings
- `ToastOverlay.show(title, body, itemName)` shows a toast, for example `ToastOverlay.show("Now Playing", "Song - Artist", "music_disc_cat")`

## Supported versions

| Minecraft | Branch | Fabric | Forge | NeoForge |
|-----------|--------|:------:|:-----:|:--------:|
| 1.20-1.20.2 | `1.20` | ✅ | ✅ | n/a |
| 1.20.3-1.20.4 | `1.20` | ✅ | ✅ | ✅ |
| 1.20.5-1.20.6 | `1.20` | ✅ | ✅ | ✅ |
| 1.21-1.21.1 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.2-1.21.3 | `1.21` | ✅ | 1.21.3 | 1.21.3 |
| 1.21.4-1.21.5 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.6-1.21.8 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.9-1.21.10 | `1.21` | ✅ | ✅ | ✅ |
| 1.21.11 | `1.21` | ✅ | ✅ | ✅ |
| 26.1-26.1.2 | `26.1` | ✅ | ✅ | ✅ |
| 26.2-26.3 | `main` | ✅ | ✅ | ✅ |

Each Minecraft line lives on its own branch and `main` is always the newest one.

NeoForge has no stable release for 1.20.2 or 1.21.2 and Forge has none for 1.21.2.


## Installing

1. Download the jar for your loader and Minecraft version from [Modrinth](https://modrinth.com/mod/proteus) or [Releases](https://github.com/OlympusMods/Proteus/releases).
2. Put it in your `mods` folder. Mods that use Proteus tell you when it is needed.

## Building

Every loader and Minecraft version is its own Gradle project, driven from one root build. Use the JDK each Minecraft version needs (the root `build.gradle` shows which; Fabric projects need Java 25 to run Gradle itself).

```sh
# one target
./gradlew :Fabric-1.21.1:build

# one loader, or everything
./gradlew buildFabric
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

# one loader, or one jar
./gradlew publishFabric
./gradlew :NeoForge-1.21.4:publishModrinth
```

Without any token the tasks only do a dry run. The Minecraft versions each jar is uploaded for come from `publish_minecraft_versions` in that project's `gradle.properties`. Modrinth keeps the environment on each version, so `publishModrinth` finishes by setting the new version to client only; change that in `publish.gradle` if the mod also runs on servers.

## Project layout

This branch holds Minecraft 1.21 to 1.21.11. The other lines are on the branches listed above.

```
common/
  client/      The toolkit: Gui, themes, Canvas, toasts and the Appearance panel (shared by every build)
  mc1_21/     Minecraft-specific glue and mixins for 1.21-1.21.1
  mc1_21_2/   ... for 1.21.2-1.21.3
  mc1_21_4/   ... for 1.21.4-1.21.5
  mc1_21_6/   ... for 1.21.6-1.21.8
  mc1_21_9/   ... for 1.21.9-1.21.10
  mc1_21_11/  ... for 1.21.11
  resources/   Language file and icon
Fabric-*/  Forge-*/  NeoForge-*/    Thin per-loader projects
publish.gradle   Modrinth and CurseForge setup shared by every project
```


Most of the toolkit lives in `common/client` and draws through the `Canvas` interface, so it has no Minecraft dependency. The version folders implement `Canvas` (`GuiCanvas`), the screen (`ProteusScreen`) and the toast hook for each game version, so a change to a widget usually only touches `client`.

When a Minecraft update breaks the shared code, copy the newest `common/mc*` folder, fix what the compiler reports, and add a project for each loader. Each folder covers the range of versions where its code compiles and its mixin targets still exist.

## Contributing

Bug reports and pull requests are welcome. For a bug, include your Minecraft version, loader and `latest.log`. Commit messages follow `type: short description` (`feat`, `fix`, `chore`, `refactor`).

## License

[GPL-3.0](LICENSE)
