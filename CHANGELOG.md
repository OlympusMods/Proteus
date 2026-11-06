# Changelog

## Unreleased

- Themes from resource packs at `assets/<namespace>/proteus/themes/<name>.json`, reloaded with F3+T. Modern and Classic are themes too and packs can change them
- Themes set colors, borders, corner radius, shadows, textures, icons, widget sizes and paddings, and every animation: spring or curve easing (back, bounce, elastic and more), speed, delay, and fade, slide, scale and spin effects
- Toasts stack: a new toast takes the corner and pushes the ones already showing away from it, up to five at a time, with a `toast_move` animation
- Liquid Glass theme: refracting glass that stretches and smears the background along its edges, with frost, color splitting at the edges, rim highlights, a glow that follows the mouse and springy motion
- Aero theme: Windows 7 glass frames in the accent color around a light client area, with glossy buttons, trackbar switches, Explorer style rows and green progress bars
- Windows 98 theme: square gray surfaces with raised and sunken bevels, a blue gradient title bar, blue block progress bars, a teal backdrop and no animation
- `glass` layer type, and `Canvas.glass` with a flat fallback for canvases that don't implement it
- Mods can ship theme files or register themes in code with `Themes.register` and `Themes.registerStyled`
- The Style setting lists every theme, and the accent picker has a "Theme color" choice that is the new default
- Removed `ModernTheme`, `ClassicTheme`, `UiPrefs.classic()` and `Theme.animated()`. Use `Themes.current()`, `Theme.motion().enabled()` and `Theme.accentColor()`
- Mods tab with a switch for every supported mod you have installed. Turning one off gives you its own menus back
- Simple Voice Chat support on Fabric, Forge and NeoForge: its voice chat menu, settings, group screens, player volumes, microphone and speaker pickers and setup screens are drawn with Proteus
- Ji AFK Cinematic support on Fabric 1.21 to 1.21.11 and 26.1 to 26.2, and on NeoForge 1.21.6 to 1.21.8: its settings and music folder pages are drawn with Proteus
- MaLiLib support on Fabric 1.20 to 1.21.11 and 26.1 to 26.2: buttons, backgrounds, checkboxes, sliders, scrollbars and tooltips of MaLiLib screens, including Litematica, MiniHUD and Tweakeroo, follow the theme. The background blurs when the theme asks for it, and a screen that fails to draw falls back to its own look
- Effects surface: the active effects in game and in the inventory are drawn by Proteus in the current theme, with a plate for harmful and beneficial effects, the time left, a pulse before an effect runs out, and `effect_in`, `effect_out`, `effect_move` and `effect_warn` motion. New `effect` widget and `effect_good` and `effect_bad` colors in themes
- Tooltips surface: item, button and chat hover tooltips get the theme's `tooltip` box and motion instead of the purple vanilla one. Off by default under Vanilla UI
- The compact effects list shows its tooltip to the right of the effect, above everything else, and it slides out with the new `effect_tip` motion and plays it backwards when it goes away
- Menus: the title screen, the pause menu and every other screen built from vanilla widgets (options, world and server lists, Create World, Realms, statistics, the mods list) are drawn with the theme's backdrop, buttons, sliders, checkboxes, text fields, tabs, lists and scrollbars, including the Mod Menu button and the icon buttons. The title screen and the pause menu ease in with the theme's `window` and `content` motion, and show at once when animations are off. Off by default, with a switch for the title screen, the pause menu and the other screens under Vanilla UI. On 1.20 to 1.20.4 the lists and the dirt background stay vanilla, and on themes with dark text the lists do too
- Menus: the server and world icons in the multiplayer and singleplayer lists have rounded corners, with a Round icons switch under Vanilla UI
- New widgets: icon toggle, and a slider that shows a label and a live level
- `Gui.codeEditor`: a code editor with line numbers, syntax colors, error marks, auto indent, tab completion and a `?` reference page, driven by a `Language` that the mod supplies
- The code editor has selection (Shift+arrows, Ctrl+arrows by word, click, drag, double and triple click), cut, copy, paste, undo and redo, word delete and block indent
- `Canvas.head` draws a player's face, and `Draw.wrap` keeps line breaks
- `ui.json` stores the theme id; the old `style` setting is read once and moved over
- Typing in a Proteus text field no longer moves the player with Inventory Move style mods
- Keys, Ctrl shortcuts and mouse buttons reach Proteus menus on 26.1, 26.2 and 26.3, and text fields take input on SDL
- Themes load on Forge, NeoForge and 26.3, and a theme with a wrong value or a failing layer is skipped with a warning instead of breaking the reload
- Chat surface: with the Chat switch on in the Vanilla UI tab, the chat messages, the input field and the scrollbar are drawn with the theme (`chat_bubble`, `chat_line`, `chat_input` and `chat_scroll` widgets). On 26.1 and newer each message is a floating bubble and the command suggestions follow the theme; a Compact bubbles switch trims the spacing. Older versions keep one flat line per message. New lines and the input field animate with `chat_message` and `chat_open` if the theme has them, and the chat falls back to vanilla if drawing fails
- Toasts fall back to the vanilla toast when the Proteus toast API is missing on a newer Minecraft
- The Toasts tab is now Vanilla UI, with a switch per surface (toasts, chat, menus, effects, tooltips). Turning Toasts off gives every game toast back, and each kind still has its own Proteus or Vanilla choice

## 1.0.0

- Animated Modern style with rounded widgets, background blur, fades and an accent color (presets, custom or chroma)
- Classic style drawn from GUI sprites, so resource packs can restyle it
- Toasts drawn by Proteus: ease in and out, item icon, resize to fit, any corner, configurable duration
- Shared Appearance settings stored in config/proteus/ui.json
- Immediate-mode widget toolkit, ProteusScreen and UiApp for mods that want their own menu
- Works as a standalone mod with its own settings screen, a keybind and a config button in Mod Menu and the mods list
- Hub: every mod that uses Proteus gets a card at the top of its menu to switch between them, with arrows when they do not fit
- Draws advancement, recipe, tutorial and system toasts in the Proteus style, set per kind in the Toasts tab
