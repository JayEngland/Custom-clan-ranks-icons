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
3. For each rank, press **Game icon** to choose another in-game icon, or **Upload** to use an image file.
4. Press **Reset** to go back to the normal icon.

Your choices are saved in your RuneLite settings, so they come back next time you log in.

### Settings

- **Keep aspect ratio**: keep the shape of uploaded images instead of stretching them to fill the icon.
- **Replace join/leave icons**: also swap the icons in clan join/leave messages.

## Notes

- Icons are matched by the rank's title. If two ranks in your clan share the same title (and so the same icon),
  they will share a replacement too; the more senior rank's choice wins.
- Because the game uses the same icon artwork for a title in every clan, a replaced icon also changes in your guest clan
  if it uses the same title.

## Building and running locally

Requires Java 11 or newer.

```
./gradlew run
```

This starts a RuneLite development client with the plugin loaded. To log in with a Jagex account, follow
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

`./gradlew shadowJar` builds `build/libs/custom-clan-rank-icons-1.0.0-all.jar`, a standalone jar of the same development client,
which you can start with `java -ea -jar custom-clan-rank-icons-1.0.0-all.jar --developer-mode`.
