# Custom Clan Rank Icons
Pick your own icon for every rank in your clan. Only you see the change.

[![Active Installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/custom-clan-rank-icons)](https://runelite.net/plugin-hub/show/custom-clan-rank-icons)

<img src="docs/example-swaps.png" alt="Examples of swapped clan rank icons" width="610" />


## Installation
1. Open RuneLite and click the **Configuration** (wrench) icon in the sidebar.
2. Click **Plugin Hub** at the bottom of the panel.
3. Search for **Custom Clan Rank Icons** and press **Install**.


## Rank icon panel
Open the panel from the gold star button in the RuneLite sidebar. It lists every rank in your clan:

- The **left icon** is the game's icon for that rank.
- The **right icon** is your replacement (a dash means it hasn't been changed).
- Next to them is the rank's title and rank number.

If your clan adds or renames ranks while you're online, press **Refresh ranks** to update the list.


## Pick an in-game icon
Press **Game icon** on any rank to open a grid of every clan rank icon in the game.
Hover an icon to see its name, and click it to use it for that rank.

Icons like Dragon, Zenyte, Monarch, the god ranks and the skill ranks are all available, whether or not your clan uses them.


## Upload your own image
Press **Upload** to use an image from your computer (PNG, JPG or GIF).

- The image is scaled to fit the rank icon's space. Turn off **Keep aspect ratio** to stretch it instead.
- Rank icons are tiny (about 11 by 11 pixels in chat), so simple, bold images with a transparent background look best.
- The image is saved in your RuneLite settings, so you can move or delete the original file afterwards.


## Where your icons show up
Your icons replace the originals everywhere the game draws them, on your screen only:

- Clan chat messages
- The clan member list
- Clan join/leave messages from RuneLite's Chat Channel plugin (optional)

Press **Reset** on a rank to go back to its normal icon, or turn the plugin off to restore every icon at once.


## Settings
| Setting | What it does |
| --- | --- |
| Show sidebar button | Hide the Clan Rank Icons button once you've set things up. Your icons stay applied. |
| Keep aspect ratio | Keep the shape of uploaded images instead of stretching them to fill the icon. |
| Replace join/leave icons | Also swap the icons in clan join/leave messages. |


## How it works
The game picks a rank's icon from its **title** (for example "Sergeant" or "Captain"), and every title has its own sprite.
When you choose an icon for a rank, the plugin finds which title that rank uses, scales your chosen image to the same size,
and tells RuneLite to draw it in place of the original sprite. Nothing is sent to the game server or to anyone else.

A few things to know:

- Icons follow titles, so two ranks in your clan that share a title also share a replacement. The more senior rank's choice wins.
- The game uses the same artwork for a title in every clan, so a replaced icon also changes in your guest clan if it uses the same title.
- If your clan changes a rank's title, your icon follows the rank to its new title automatically.
