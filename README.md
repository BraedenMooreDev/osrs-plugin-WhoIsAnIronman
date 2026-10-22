# Who is an Ironman

**Who is an Ironman** is a RuneLite plugin for Old School RuneScape that identifies player account types and displays the corresponding Ironman helmet icons next to player names in the right-click context menu.

## Features

- **Ironman Helmet Icons:** Identify Ironman, Hardcore Ironman, and Ultimate Ironman accounts at a glance.
- **Previous Account Types:** Detect former statuses, such as Hardcore Ironmen who lost their status or Ironmen who de-ironed, when identifiable through the Hiscores.
- **Chat Information:** Display players' account types and detected previous statuses in chat.
- **Player Lookup:** Look up a player's account type directly from their right-click menu.
- **Automatic Lookup:** Optionally look up all players on a tile when right-clicking it.

## How to Use

### Look Up a Player

1. Open the plugin configuration and enable **Player 'Who is' option**.
2. Right-click a player and select **Who is**.
3. Once the lookup completes, the player's account type can be displayed in chat and their helmet icon can appear in the context menu.

### Automatically Look Up Players

Enable **Lookup on right click** in the plugin configuration to look up players on a tile whenever you right-click it. This is useful for identifying multiple nearby players without selecting each one individually.

### Customize Display

Use the plugin configuration to control whether lookup results appear in chat, whether helmet icons appear in the right-click context menu, and whether previous account types are displayed.

- **Chat information:** Configure chat messages, including whether Normal accounts and previous account types are shown.
- **Context menu helmets:** Configure helmet icons and whether previous account types are displayed alongside the current account type.

## Notes

- Account identification uses the OSRS Hiscores.
- Lookups run asynchronously, so results may take a moment to appear.
- Previous account types depend on available Hiscores data and may not always be detectable.
- Players without accessible Hiscores data may not be identifiable.

