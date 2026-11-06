# Proteus

A shared UI toolkit for Olympus mods: animated widgets, themes and toasts.

Proteus is the UI library behind Olympus mods, and it also works on its own. The theme, accent color and toast placement you pick apply to every mod that uses it, and it can draw the game's own toasts in the same style.

- **Modern style** with rounded widgets, background blur, smooth animations and an accent color (presets, a custom hue, or a chroma that cycles through the rainbow)
- **Liquid Glass style** where windows, buttons, toggles, toasts and tooltips are glass: the background bends at the edges with a bit of color splitting, a bright rim catches the light, buttons light up where the mouse is and everything moves with a bit of bounce
- **Aero style** that looks like Windows 7: frosted glass window frames tinted with your accent color, a light client area, glossy gray buttons that turn blue on hover, green progress bars and quick fades
- **Windows 98 style** with square gray surfaces, raised and sunken bevels that flip when you press a button, a blue gradient title bar, blocky progress bars and a teal backdrop. No animation
- **Classic style** that brings back the old Minecraft look: dirt background, bevelled gray buttons, no animation. It is drawn from GUI sprites under `proteus:classic`, so resource packs can restyle it
- **Themes** from resource packs and mods. Modern and Classic are themes themselves, and a theme can change colors, borders, corner radius, shadows, textures, widget sizes and every animation (easing, speed, fade, slide, scale, spin). See [Themes](#themes)
- **Toasts** drawn by Proteus: they stack, with a new one pushing the others out of the corner, ease in and out, show an item icon, grow to fit their text, appear over menus and in game, and can sit in any corner
- **One menu for every mod**: each mod built on Proteus gets a card at the top of its menu, so you can go from Orpheus to Mneme to the Proteus settings without closing anything. When there are more cards than fit, the arrows page through them
- **Mods tab** that lists the mods Proteus can draw menus for, with a switch for each. Turn one off and its own menus come back
- **Vanilla UI** tab with a switch for each part of the game's own interface that Proteus can draw: toasts, chat, menus and effects. Toasts are on by default and the rest are off. Under Toasts, advancements, recipes, tutorial hints and system notices can each be set back to the vanilla toast
- One toolkit across every supported version: widgets are drawn through a small `Canvas` interface, and each Minecraft range only implements that interface

Open the settings with the Proteus key (unbound by default, set it under Controls, Miscellaneous), with the config button in Mod Menu or the mods list, or from the Proteus card in any mod's menu. Settings are stored in `config/proteus/ui.json`.

## Themes

A theme is a JSON file in a resource pack at `assets/<namespace>/proteus/themes/<name>.json`, and its id is `<namespace>:<name>`. It shows up in the Style list under Appearance, and F3+T reloads it. Shipping a file with the id of an existing theme (`proteus:modern` or `proteus:classic`) changes that theme instead.

A theme starts from its `parent` (Modern when it has none) and only lists what it changes. This one is Modern with an orange accent and rounder buttons:

```json
{
  "name": "Sunset",
  "parent": "proteus:modern",
  "accent": { "color": "#FF7A45" },
  "widgets": { "button": { "vars": { "radius": 8 } } }
}
```

Objects merge with the parent, and anything else (numbers, strings, lists) replaces the parent's value. Read [`modern.json`](common/src/main/resources/assets/proteus/proteus/themes/modern.json) and [`classic.json`](common/src/main/resources/assets/proteus/proteus/themes/classic.json) to see every key in use. If something in a theme is broken, Proteus logs it under `Theme` in `latest.log` and draws that part the way the parent does.

The top-level keys are:

- `name` and `description`: shown in the Style list. Either can be a lang key
- `accent`: `color` is the theme's own accent, used when the player picks "Theme color". `"locked": true` ignores the player's accent and hides the picker
- `colors`: `text`, `text_secondary`, `text_disabled`, `title`, `danger`, `toast_title`, plus any names of your own for use in widgets
- `text`: `shadow` (true or false) and `title_align` (`left` or `center`)
- `segments.sliding`: segmented controls slide a thumb (Modern) or draw separate buttons (Classic)
- `backdrop.vanilla_blur`: use the game's blurred background behind menus
- `icons`: `"draw": "painter"` draws the built-in icons, `"draw": "sprite"` draws `sprite` (`{name}` is replaced) for each one, and `map` overrides single icons with `{ "sprite": "...", "w": 8, "h": 8 }`
- `sizes`, `motion` and `widgets`, below

### Widgets

Each entry in `widgets` draws one part of the UI: `backdrop`, `window`, `card`, `button` (with the variants `button.primary`, `button.danger`, `button.ghost` and `button.segment`, which start from `button`), `segment_track`, `segment_thumb`, `toggle`, `checkbox`, `inset`, `field`, `row`, `progress`, `scrollbar`, `tooltip`, `toast` and `swatch`.

A widget has `vars`, a list of `layers` drawn in order, and for buttons and `segment_track` a `text_color`. Every value is an expression, so `"radius": 6`, `"color": "#FF101010"` and `"color": "mix(text, accent, hover)"` all work. Vars are worked out first and in order, so layers and later vars can use them.

Layer types are `fill`, `round`, `ring`, `panel` (a fill with a one pixel border), `gradient` (`top` and `bottom`), `shadow` (`spread`, `alpha`), `sprite`, `tiled` (`texture`, with an optional `fallback` list of layers when it is missing), `icon`, `check`, `text`, `group` (its own `layers`), `widget` (draws another widget, with `vars` to override) and `glass`. Every layer takes:

- `x`, `y`, `w`, `h`: relative to the widget, defaulting to all of it
- `inset`: shrink every side
- `radius`, `color`, `border`, `opacity`
- `when`: skip the layer when this is false
- `rotate` (degrees) and `scale`, around the layer's center

`glass` is a piece of glass that bends what is under it. It copies the screen behind the layer and runs it through a shader, so it shows the world, the panorama and any widget drawn before it. It takes `radius`, `opacity` and `color` (the tint, where the alpha is how strong it is), plus:

- `blur`: how frosted it is, in GUI pixels (0 is clear glass)
- `refraction`: how far the edge bends the background, in GUI pixels. Negative values make it look sunken, which is what the Liquid Glass theme uses for fields
- `depth`: how wide the curved edge is
- `dispersion`: how much the colors split at the edge (0 to 1)
- `zoom`: how much the edge stretches the background along itself, in GUI pixels, so text and lines under the top and bottom edges widen and curve
- `smear`: how far the background is streaked along the edge, in GUI pixels
- `highlight`: how bright the rim and the shine are
- `light`: where the light comes from, in degrees clockwise from the top (`-45` is the top left)
- `saturation`: 1 keeps the colors behind it, higher makes them stronger
- `glow`: a soft light that follows the mouse, for hover and press

If the shader can't load, glass is drawn as a flat rounded fill in its tint color.

`sprite`, `texture`, `text`, `name` (icons) and `align` are plain text, unless they start with `=`, which makes the rest an expression: `"sprite": "=if(focus > 0.5, \"mypack:field_on\", \"mypack:field\")"`. Sprites are GUI sprites under `textures/gui/sprites/`, so a `.mcmeta` next to the png can nine-slice or tile it.

Expressions have numbers, colors (`#RRGGBB` or `#AARRGGBB`), `"text"`, `+ - * / %`, comparisons, `&& || !`, and the functions `mix`, `lighten`, `darken`, `alpha`, `hsl`, `min`, `max`, `clamp`, `floor`, `round`, `step`, `if` and `exists("namespace:path")`. A name is looked up in the widget's vars, then the widget's state, then `colors`. The state is `w`, `h`, `hover`, `press`, `selected`, `enabled`, `focus`, `on`, `highlighted`, `value`, `time`, `thumb_y`, `thumb_h`, `color` (swatches), `on_label` and `off_label` (toggles) and `in_world` (backdrop), depending on the widget. States like `hover` go from 0 to 1 as the animation plays, which is what makes colors fade.

### Sizes

`sizes` changes how big things are inside a widget, not where a menu puts them: `toggle` (`width`, `height`), `segment` (`pad`, `gap`), `field.pad`, `area.pad`, `tooltip` (`pad`, `offset_x`, `offset_y`), `scrollbar.width` and `toast` (`margin`, `pad`, `icon`, `min_width`, `max_width`, `max_lines`).

### Motion

`motion` has `enabled`, `speed` (multiplies every animation) and one entry per moment: `window`, `content`, `card_bar`, `toast_in`, `toast_out`, `toast_resize`, `toast_move` (toasts making room for a new one), `tooltip`, `hover`, `press`, `select`, `toggle`, `checkbox`, `row`, `segment`, `focus`, `knob` and `scroll`.

```json
"motion": {
  "window": { "easing": "back", "overshoot": 1.6, "duration": 0.35,
              "effects": { "fade": true, "scale": { "from": 0.9 }, "spin": { "degrees": 8 } } },
  "hover": { "easing": "spring", "rate": 10 }
}
```

`easing` is `spring` (with `rate`), or a curve with a `duration` in seconds: `linear`, `ease_in`, `ease_out`, `ease_in_out`, `back` (`overshoot`), `bounce` or `elastic` (`period`). `delay` waits before a curve starts. `effects` apply to windows, content, the card bar, toasts and tooltips: `fade`, `slide` (`x`, `y`, and `"relative": true` to move by the element's size), `scale` (`from`) and `spin` (`degrees`). Each effect can take its own `easing` and `speed`. The Animations switch in Appearance turns all of it off.

## Supported mods

When a supported mod is installed it shows up in the Mods tab. Its menus are drawn with Proteus widgets and follow your theme and accent color.

- **Simple Voice Chat** (2.6): the voice chat menu, settings, groups, player volumes, microphone and speaker pickers and the setup screens. It is available on every version Simple Voice Chat has a client build for: 1.20.1, 1.21 to 1.21.1, 1.21.4 to 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1 and 26.2 to 26.3
- **MaLiLib** (and the mods built on it, like Litematica, MiniHUD and Tweakeroo): buttons, screen backgrounds, checkboxes, sliders, scrollbars and tooltips are drawn with the Proteus theme, so every MaLiLib screen follows your theme and accent color. The layouts stay MaLiLib's own, only the look changes. It is Fabric only, on 1.20 to 1.21.11 and 26.1 to 26.2, because MaLiLib has no Forge or NeoForge builds. If a screen fails to draw, it falls back to MaLiLib's own look. Turn it off in the Mods tab to get the original look back
- **Ji AFK Cinematic** (2.4.0): its settings, with the music folder pages, on Fabric 1.21 to 1.21.11 and 26.1 to 26.2. The report form and the support links are left out. They stay in Ji's own screen, which comes back when you turn Ji off in the Mods tab. On NeoForge it only works on 1.21.6 to 1.21.8, because Ji's other NeoForge builds rename their classes for each Minecraft version, so there is nothing stable to build against. Proteus shows its own copy of the settings screen as a subclass of Ji's, so Ji still sees its settings open and keeps its shortcuts out of the way while you rebind them

Nothing in the other mod is patched. When it opens one of its screens, Proteus puts its own in the same place and reads and writes the other mod's own settings, so the config files stay the same. A mod is supported by adding an entry in `Mods` and a `ScreenTakeover` that turns its screens into Proteus ones.

## For mod authors

Add Proteus as a dependency (the artifact name follows the jar: `proteus-<loader>-<minecraft versions>`):

```groovy
repositories { mavenLocal() }   // until the library is on a public Maven repository

dependencies {
    modImplementation "com.github.olympus.proteus:proteus-fabric-1.21-1.21.1:1.1.0"   // Fabric
    implementation "com.github.olympus.proteus:proteus-neoforge-1.21-1.21.1:1.1.0"    // NeoForge
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
- `Gui` is the immediate-mode toolkit: buttons, steppers, segmented controls, sliders, switches, single-line text fields, a multi-line text area with word wrap (`textArea`), a code editor (`codeEditor`, see below), scroll areas and tooltips, all themed. `focus` and `insertAtFocus` let a screen move the cursor or write into the field being edited
- `Gui.codeEditor` is a text area for scripts: line numbers, syntax colors, error marks, auto indent, a completion popup (Tab or Enter accepts, Up and Down pick, Esc closes, Ctrl+Space lists everything) and a `?` button at the bottom right that opens a reference page. Editing works like an IDE: Shift+arrows, Ctrl+arrows (by word) and Ctrl+Shift+arrows select, click and drag select, double click picks a word, triple click a line, Ctrl+A/C/X/V, Ctrl+Z and Ctrl+Y or Ctrl+Shift+Z undo and redo, Ctrl+Backspace and Ctrl+Delete remove a word, and Tab or Shift+Tab indents the selected lines. It knows no language of its own. You hand it a `Language` built with `Language.builder(title)`, listing an `Entry` for each command with its usage, summary, detail, examples and values to complete, plus an optional `comment` prefix, a `block(open, close)` pair for indenting, a `highlighter` and a `validator` that returns `Problem(line, message)` for bad lines
- `AppearancePanel` is the Appearance tab from the Proteus card, for a mod that wants the same settings inside its own screen
- `ToastOverlay.show(title, body, itemName)` shows a toast, for example `ToastOverlay.show("Now Playing", "Song - Artist", "music_disc_cat")`
- `Overlays.add(overlay)` draws something on the HUD every frame, such as pinned notes. An `Overlay` says whether it is `visible()` and draws itself through a `Canvas`
- `Icons.register("modid:name", classicWidth, classicHeight, painter)` adds a mod's own icon. The Modern style calls the painter, and the Classic style draws the sprite `modid:classic/icon/name`
- `Themes.current()` is the theme the player picked. A mod can ship its own theme as a JSON file in its jar, the same way a resource pack does, or register one in code: `Themes.registerStyled("modid:name", json)` for a JSON spec, `Themes.registerStyled("modid:name", json, MyTheme::new)` for a `StyledTheme` subclass that overrides some of its draw methods, or `Themes.register("modid:name", MyTheme::new)` for a `Theme` written from scratch. Packs can extend or change the first two kinds, and only replace the third
- A mod's own sizes and animations can be themed too: `gui.sizes().get("modid:row", 24)` and `gui.anim(key, target, gui.motion().get("modid:slide", 14f))` read `"modid:row"` from `sizes` and `"modid:slide"` from `motion`, and fall back to the given values
- `theme.danger()` and `theme.accentColor()` give the theme's error and accent colors

## Supported versions

| Minecraft | Folder | Fabric | Forge | NeoForge |
|-----------|--------|:------:|:-----:|:--------:|
| 1.20-1.20.2 | [`1.20`](versions/1.20) | ✅ | ✅ | n/a |
| 1.20.3-1.20.4 | [`1.20.3`](versions/1.20.3) | ✅ | ✅ | ✅ |
| 1.20.5-1.20.6 | [`1.20.5`](versions/1.20.5) | ✅ | ✅ | ✅ |
| 1.21-1.21.1 | [`1.21`](versions/1.21) | ✅ | ✅ | ✅ |
| 1.21.2-1.21.3 | [`1.21.2`](versions/1.21.2) | ✅ | ✅ (1.21.3) | ✅ (1.21.3) |
| 1.21.4-1.21.5 | [`1.21.4`](versions/1.21.4) | ✅ | ✅ | ✅ |
| 1.21.6-1.21.8 | [`1.21.6`](versions/1.21.6) | ✅ | ✅ | ✅ |
| 1.21.9-1.21.10 | [`1.21.9`](versions/1.21.9) | ✅ | ✅ | ✅ |
| 1.21.11 | [`1.21.11`](versions/1.21.11) | ✅ | ✅ | ✅ |
| 26.1-26.1.2 | [`26.1`](versions/26.1) | ✅ | ✅ | ✅ |
| 26.2-26.3 | [`26.2`](versions/26.2) | ✅ | ✅ | ✅ |

Each range of Minecraft versions that one jar per loader can cover has a folder under `versions`, named after the first version of the range.

NeoForge has no stable release for 1.20.2 or 1.21.2 and Forge has none for 1.21.2.


## Installing

1. Download the jar for your loader and Minecraft version from [Modrinth](https://modrinth.com/mod/proteus) or [Releases](https://github.com/OlympusMods/Proteus/releases).
2. Put it in your `mods` folder. It works on its own, and mods that use Proteus tell you when it is needed. Fabric also needs Fabric API.

## Building

Everything lives on one branch. A build assembles one project per Minecraft range and loader under `build/targets/<range>/<loader>` and runs that project's own Gradle wrapper, so each one gets the Gradle it needs. Use the JDK each loader needs (`settings.gradle` reads them from `versions/*/version.properties`; Fabric needs Java 25 to run Gradle itself).

```sh
# one loader on one range
./gradlew :1.21.4:fabric:build

# every loader on one range
./gradlew :1.21.4:build

# narrow the whole build
./gradlew build -PonlyVersions=1.21.4,1.21.6 -PonlyLoaders=fabric

# everything
./gradlew build
```

Finished jars are collected into `build/dist` and named `proteus-<loader>-<minecraft versions>-<version>.jar`. The mod version is set in the root `gradle.properties`. `./gradlew assemble` only lays the projects out. Source files in a project are symlinks to the real files, so edits go to the right place. On Windows they are copied instead, so edit the files under `core`, `loaders` and `versions`.

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
./gradlew :1.21.4:neoforge:publishModrinth
```

Without any token the tasks only do a dry run. The Minecraft versions each jar is uploaded for come from `publish_minecraft_versions` in each loader's `gradle.properties`. Modrinth keeps the environment on each version, so `publishModrinth` finishes by setting the new version to client only; change that in `publish.gradle` if the mod also runs on servers.

## Project layout

```
core/                       Code and resources every range and loader shares
loaders/<loader>/           Files every range of that loader shares
versions/<range>/common/    What changed in this range, for every loader
versions/<range>/<loader>/  What changed in this range, for one loader
versions/<range>/version.properties   Loaders in the range and the JDK that runs each Gradle build
versions.txt                The ranges, oldest first
publish.gradle              Modrinth and CurseForge setup shared by every project
```

A project is the layers stacked in that order: `core`, then `loaders/<loader>`, then every range from the oldest up to its own, each with `common` before the loader folder. A later file replaces an earlier one with the same path, and an empty `name.removed` file deletes `name`. So a range only holds the files that differ from the range before it, and a Minecraft range inherits everything from the older ones.

`core` holds the toolkit in `client/ui`, which has almost no version-specific code. The glue for a Minecraft version (`GuiCanvas`, `ProteusScreen`, the toast and HUD hooks, mixins) sits in the oldest range that needs it and is replaced in the ranges where it changes. Build scripts and `gradle.properties` work the same way. The tests are in `loaders/fabric`, because only the Fabric projects run them.

To fix something that every range shares, edit it in `core`. To see which ranges have their own copy of a file, search `versions` for its name, for example `find versions -name GuiCanvas.java`. A fix to a file that several ranges override has to be made in each override.

When a Minecraft update breaks the code, add a folder to `versions` and to `versions.txt`, put only the files the compiler complains about in it, and narrow the range of the one before it if its jar no longer loads. A range covers the versions where one jar per loader still loads and its mixin targets still exist.

## Contributing

Bug reports and pull requests are welcome. For a bug, include your Minecraft version, loader and `latest.log`. Commit messages follow `type: short description` (`feat`, `fix`, `chore`, `refactor`).

## License

[GPL-3.0](LICENSE)
