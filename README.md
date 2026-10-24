# Custom Clan Rank Icons

A RuneLite plugin that lets you pick your own icon for each rank in your Old School RuneScape clan.
The change is client-side only: you see your icons, everyone else still sees the normal ones.

## Features

- A side panel listing every rank in your clan, with the original icon next to your replacement.
- Pick any other clan rank icon from the game, or upload your own image (PNG, JPG or GIF).
- Uploaded images are scaled to fit the rank icon's space automatically.
- Replacements show up wherever the game draws that rank's icon, such as clan chat and the clan member list.
- Optionally also replaces the icons in the clan join/leave messages from RuneLite's Chat Channel plugin.
- Reset any rank back to its normal icon with one click, or turn the plugin off to restore everything.

## How to use

1. Log in and join your clan.
2. Open the **Clan Rank Icons** panel from the RuneLite sidebar (the gold shield).
3. Each row is one rank in your clan. On the left is the game's icon for that rank, next to it is your replacement
   (a dash means you haven't changed it), then the rank's title and number.
4. Press **Game icon** to open a grid of every clan rank icon in the game. Hover an icon to see its name, and click one
   to use it for that rank.
5. Or press **Upload** to choose an image file from your computer.
6. Press **Reset** to go back to the normal icon.

The new icon appears straight away in clan chat and the clan member list. Your choices are saved in your RuneLite
settings, so they come back next time you log in. If your clan adds or renames ranks while you're online, press
**Refresh ranks** to update the list.

### Settings

- **Show sidebar button**: hide the Clan Rank Icons button once you've set things up. Your icons stay applied.
- **Keep aspect ratio**: keep the shape of uploaded images instead of stretching them to fill the icon.
- **Replace join/leave icons**: also swap the icons in clan join/leave messages.

### Tips for custom images

- Rank icons are tiny (about 11 by 11 pixels in chat), so simple, bold images with a transparent background look best.
- Semi-transparent edges are snapped to fully visible or fully hidden, because the game can't draw partial transparency.
- Uploaded images are stored in your RuneLite settings, so you can delete or move the original file afterwards.

## How it works

The game picks a rank's icon from its **title** (for example "Sergeant" or "Captain"), and every title has its own
sprite in the game cache. When you choose an icon for a rank, the plugin:

1. Reads your clan's settings to find which title that rank uses, and which sprite the game draws for that title.
2. Scales your chosen image to the same size as the original sprite.
3. Gives the RuneLite client a sprite override for that sprite. The game then draws your image everywhere it would
   have drawn the original, on your screen only.
4. Swaps the matching icon in RuneLite's chat icon list as well, so join/leave messages match.

Nothing is sent to the game server or to anyone else. The plugin checks your clan's ranks every few seconds, so if a
rank's title changes your icon moves with it. Turning the plugin off removes every override and restores the
original icons.

## Notes

- Because icons follow titles, two ranks in your clan that share a title also share a replacement; the more senior
  rank's choice wins.
- The game uses the same artwork for a title in every clan, so a replaced icon also changes in your guest clan if it
  uses the same title.

## Building and running locally

Requires Java 11 or newer.

```
./gradlew run
```

This starts a RuneLite development client with the plugin loaded. To log in with a Jagex account, follow
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

`./gradlew shadowJar` builds a standalone `-all.jar` in `build/libs`, a jar of the same development client,
which you can start with `java -ea -jar <that jar> --developer-mode`.
