# Changelog

## 1.1.0

- Themes from resource packs at `assets/<namespace>/proteus/themes/<name>.json`, reloaded with F3+T. Modern and Classic are themes too and packs can change them
- Themes set colors, borders, corner radius, shadows, textures, icons, widget sizes and paddings, and every animation: spring or curve easing (back, bounce, elastic and more), speed, delay, and fade, slide, scale and spin effects
- Mods can ship theme files or register themes in code with `Themes.register` and `Themes.registerStyled`
- The Style setting lists every theme, and the accent picker has a "Theme color" choice that is the new default
- Removed `ModernTheme`, `ClassicTheme`, `UiPrefs.classic()` and `Theme.animated()`. Use `Themes.current()`, `Theme.motion().enabled()` and `Theme.accentColor()`
- `ui.json` stores the theme id; the old `style` setting is read once and moved over

## 1.0.0

- Animated Modern style with rounded widgets, background blur, fades and an accent color (presets, custom or chroma)
- Classic style drawn from GUI sprites, so resource packs can restyle it
- Toasts drawn by Proteus: ease in and out, item icon, resize to fit, any corner, configurable duration
- Shared Appearance settings stored in config/proteus/ui.json
- Immediate-mode widget toolkit, ProteusScreen and UiApp for mods that want their own menu
- Works as a standalone mod with its own settings screen, a keybind and a config button in Mod Menu and the mods list
- Hub: every mod that uses Proteus gets a card at the top of its menu to switch between them, with arrows when they do not fit
- Draws advancement, recipe, tutorial and system toasts in the Proteus style, set per kind in the Toasts tab
