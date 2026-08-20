# Piko Client

A lightweight, legitimate **Minecraft 1.8.9 PvP client** built as a Forge mod. Piko focuses on
high frame rates, a fully customisable HUD, smooth combat visuals and quality of life features
that are safe to use on normal PvP servers.

* **Minecraft:** 1.8.9
* **Loader:** Forge 1.8.9 (11.15.1.2318)
* **Java:** 8
* **Build:** Gradle with ForgeGradle 2.1, IntelliJ IDEA friendly

Open the mod menu in game with **Right Shift**.

---

## Fair play

Piko is a rendering and information client. It deliberately contains **no** kill aura, aim assist,
reach modification, velocity or anti knockback changes, auto clicker, trigger bot, fly, speed,
scaffold, x-ray, ESP, packet manipulation, blink, timer, fast place or no slow bypass.

The two combat related readouts follow the same rule:

* **Reach Display** measures the distance of a hit that vanilla already allowed. It never extends
  the attack range.
* **Combo Counter** counts hits that already happened. It never influences combat.
* **CPS Counter** counts clicks the player physically made. Piko cannot generate a click.
* **Toggle Sprint / Toggle Sneak** only hold Minecraft's own key bindings, so movement, packets
  and speed are exactly what vanilla produces.

---

## Installation

1. Install **Java 8** and the **Minecraft 1.8.9 Forge** client
   (`forge-1.8.9-11.15.1.2318-1.8.9-installer.jar`, "Install client").
2. Launch the `1.8.9-forge...` profile once so the folders are created.
3. Drop `PikoClient-1.0.jar` into `.minecraft/mods/`.
4. Start the game. The Piko main menu appears and `.minecraft/piko/` is created.
5. Press **Right Shift** in game to open the mod menu.

The jar is a normal Forge mod. It is client side only and never needs to be installed on a server.

### Building from source

```bash
./gradlew build          # produces build/libs/PikoClient-1.0.jar
./gradlew runClient      # launches a development client
./gradlew setupDecompWorkspace idea   # IntelliJ workspace with decompiled sources
```

Gradle must run on **Java 8** (`JAVA_HOME` pointing at a JDK 8 installation). ForgeGradle 2.1 does
not support newer JDKs.

---

## Interface

### Mod menu (Right Shift)

A sidebar selects the category, every module is a card with an ON/OFF switch, and clicking a card
opens its settings. The search box filters across every category as you type: `cross` finds
Crosshair, `fps` finds the FPS Counter and the FPS Boost presets.

Categories: **HUD**, **PvP**, **Visual**, **Performance**, **GUI**, plus **Profiles**.

### HUD editor

Reachable from the mod menu sidebar or from a keybind of its own.

* Drag elements to move them
* Drag the corner handle, or scroll over an element, to scale it
* Right click an element to enable or disable it
* `R` resets the element under the cursor
* Snapping to screen edges, screen centre and to the edges of other elements, with a dashed guide
  showing the active alignment
* Positions are stored as a fraction of the screen, so a layout survives a resolution change

### Profiles

`Default`, `Bedwars`, `SkyWars`, `Boxing`, `Practice` and `Hypixel` are created on first launch.
Profiles can be created, renamed, switched, deleted, exported to `piko/exports/` and imported back.

---

## Modules

### HUD

| Module | Notes |
| --- | --- |
| FPS Counter | Minecraft's own frame counter, prefix/suffix/custom label |
| CPS Counter | Left, right or combined, three display styles |
| Keystrokes | WASD, mouse buttons with CPS, space bar, animated presses |
| Ping Display | Latency with optional colour by quality |
| Coordinates | X/Y/Z, one or three lines, optional direction |
| Armor Status | Helmet to boots with durability numbers or percentage |
| Potion Effects | Icons, level and remaining duration |
| Pack Display | Active resource pack with its icon |
| Clock | 12 or 24 hour, optional seconds |
| Memory Usage | Heap percentage with a usage bar |

### PvP

| Module | Notes |
| --- | --- |
| Toggle Sprint | Toggle or always sprint, own sprint key, HUD indicator |
| Toggle Sneak | Toggle sneak with status indicator |
| Crosshair | Width, height, thickness, gap, dot, outline, colours, opacity, six presets |
| Block Hit Animation | Default, 1.7, Slide, Swing, Push and Piko styles plus item offsets and scale |
| Hit Color | Recolours the damage flash on players |
| Combo Counter | Consecutive hits on one target, display only |
| Reach Display | Distance of the last landed hit, display only |
| Direction HUD | N/NE/E/SE/S/SW/W/NW with optional yaw |

### Visual

| Module | Notes |
| --- | --- |
| Time Changer | Client side day, night, custom tick and more |
| Fullbright | Gamma based brightness, nothing sent to the server |
| Motion Blur | Frame blending, disabled automatically in Maximum FPS mode |
| Item Physics | Dropped items rest flat on the ground |
| Perspective | Free look: the camera turns, the body does not |
| Zoom | Built in zoom on `C`, smooth camera and scroll adjustment |
| Name Tags | Scale, background, colour, distance limit |
| Chat | Transparent background, opacity, timestamps, custom font, scale, smooth animation |
| 1.7 Animations | Block hit, item holding, bow, rod, eating and swing |
| Low Fire | Moves the fire overlay out of the way |
| PvP Visuals | Clear water, no pumpkin blur, no boss bar, no hurt cam, no weather, fast graphics |

### Performance

| Module | Notes |
| --- | --- |
| FPS Boost | Quality, Balanced and Maximum FPS presets; every value stays editable afterwards |
| Particle Settings | Per type toggles and a 0.0x to 5.0x multiplier |
| Entity Rendering | Separate render distances for players, mobs and dropped items |
| Animation Settings | Disable animated water, lava, fire, portal and other textures |
| Chunk Settings | Render distance, VBOs, smooth lighting |
| Memory Optimization | Threshold based collection and cleanup on world leave |

### GUI

HUD Editor, Piko Theme (accent colours, corner radius, panel opacity), GUI Scale, Animations and
the Piko main menu.

---

## Configuration

Everything lives in `.minecraft/piko/`:

```
piko/config.json      client state such as the active profile
piko/modules.json     enabled flags and every setting
piko/hud.json         HUD element positions
piko/keybinds.json    client hotkeys and module keybinds
piko/profiles/        one json file per profile
piko/exports/         exported profiles, also the import source
```

Changing a setting only marks the configuration dirty. The actual write happens at most once every
few seconds, when a Piko screen closes, and on shutdown, so nothing touches the disk from the
render loop.

---

## Project layout

```
src/main/java/piko/
  PikoClient.java          mod entry point, manager wiring
  event/                   interface based event bus and the Forge bridge
  module/                  Module, ModuleManager, HudModule and every feature
    hud/ pvp/ visual/ performance/ gui/
  setting/                 Boolean, Number, Slider, Mode, Color, Keybind and String settings
  config/                  config and keybind persistence
  profile/                 profile manager
  gui/                     mod menu, main menu, chat renderer, widgets
    components/ hud/
  render/                  2D drawing, first person hand, item and particle renderers
  animation/               easing and time based value smoothing
  font/                    glyph atlas font renderer
  util/                    small shared helpers
src/main/resources/META-INF/piko_at.cfg   access transformer
```

### Design notes

* **Events.** Listeners are stored per event type, and modules are only registered while they are
  enabled, so a disabled module never runs a line of code. Event objects are reused rather than
  allocated per frame.
* **Rendering.** Rounded panels come from a precomputed corner table, text from a glyph atlas that
  is uploaded once, and the scaled resolution is cached instead of rebuilt every frame.
* **Access transformer.** Piko widens a handful of vanilla rendering internals
  (`ItemRenderer`, `EntityRenderer`, `EffectRenderer`, `TextureMap`, `RenderManager`,
  `RendererLivingEntity`, `GuiNewChat`) so the first person animations, particle filtering and hit
  colour can be implemented without a coremod or a mixin loader.
