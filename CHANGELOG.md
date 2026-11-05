# Changelog

## 1.1.0

- Themes from resource packs at `assets/<namespace>/proteus/themes/<name>.json`, reloaded with F3+T. Modern and Classic are themes too and packs can change them
- Themes set colors, borders, corner radius, shadows, textures, icons, widget sizes and paddings, and every animation: spring or curve easing (back, bounce, elastic and more), speed, delay, and fade, slide, scale and spin effects
- Toasts stack: a new toast takes the corner and pushes the ones already showing away from it, up to five at a time, with a `toast_move` animation
- Liquid Glass theme: refracting glass that stretches and smears the background along its edges, with frost, color splitting at the edges, rim highlights, a glow that follows the mouse and springy motion
- Aero theme: Windows 7 glass frames in the accent color around a light client area, with glossy buttons, trackbar switches, Explorer style rows and green progress bars
- `glass` layer type, and `Canvas.glass` with a flat fallback for canvases that don't implement it
- Mods can ship theme files or register themes in code with `Themes.register` and `Themes.registerStyled`
- The Style setting lists every theme, and the accent picker has a "Theme color" choice that is the new default
- Removed `ModernTheme`, `ClassicTheme`, `UiPrefs.classic()` and `Theme.animated()`. Use `Themes.current()`, `Theme.motion().enabled()` and `Theme.accentColor()`
- Mods tab with a switch for every supported mod you have installed. Turning one off gives you its own menus back
- Simple Voice Chat support: its voice chat menu, settings, group screens, player volumes, microphone and speaker pickers and setup screens are drawn with Proteus
- Ji AFK Cinematic support on Fabric: its settings and music folder pages are drawn with Proteus
- New widgets: icon toggle, and a slider that shows a label and a live level
- `Gui.codeEditor`: a code editor with line numbers, syntax colors, error marks, auto indent, tab completion and a `?` reference page, driven by a `Language` that the mod supplies
- The code editor has selection (Shift+arrows, Ctrl+arrows by word, click, drag, double and triple click), cut, copy, paste, undo and redo, word delete and block indent
- `Canvas.head` draws a player's face, and `Draw.wrap` keeps line breaks
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
