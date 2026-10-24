/*
 * Copyright (c) 2026, JayEngland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.clanrankicons;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.MessageNode;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Custom Clan Rank Icons",
	description = "Pick your own icon for each rank in your clan. Only you can see the changes.",
	tags = {"clan", "rank", "icon", "custom", "chat"}
)
public class ClanRankIconsPlugin extends Plugin
{
	/**
	 * Before 1.1, choices were saved per rank only; these get moved to the first clan seen.
	 */
	private static final String LEGACY_RANK_KEY_PREFIX = "rank_";
	private static final String PLAYER_ICONS_KEY = "playerIcons";
	static final int MIN_RANK = -1;
	static final int MAX_RANK = 127;
	private static final int CHAT_ICON_SIZE = 11;
	private static final int RECHECK_TICKS = 5;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ChatIconManager chatIconManager;

	@Inject
	private ClanRankIconsConfig config;

	private ClanRankIconsPanel panel;
	private NavigationButton navButton;

	/**
	 * Sprite ids we have put into the client's sprite overrides.
	 */
	private final Set<Integer> overriddenSprites = new HashSet<>();
	/**
	 * Clan titles whose RuneLite chat icon (used in join/leave messages) we replaced.
	 */
	private final Set<ClanTitle> overriddenChatIcons = new HashSet<>();
	/**
	 * What is currently applied, so we only touch the client when something changes.
	 */
	private Map<ClanTitle, IconChoice> appliedPlan = Collections.emptyMap();
	private boolean dirty;
	private int tickCounter;
	/**
	 * Config key part for the clan we're in, so each clan keeps its own icons. Null when not in a clan.
	 */
	private volatile String clanKey;
	/**
	 * Chat icon ids (from ChatIconManager) for players with their own icon, keyed by normalised name.
	 * Chat icons can't be unregistered, so ids no longer in use are kept and reused.
	 */
	private final Map<String, Integer> playerChatIcons = new HashMap<>();
	private final List<Integer> spareChatIcons = new ArrayList<>();
	private boolean playerIconsDirty = true;

	@Provides
	ClanRankIconsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanRankIconsConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new ClanRankIconsPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Clan Rank Icons")
			.icon(ImageUtil.loadImageResource(getClass(), "panel_icon.png"))
			.priority(10)
			.panel(panel)
			.build();
		if (config.showSidebarButton())
		{
			clientToolbar.addNavigation(navButton);
		}

		clientThread.invokeLater(() ->
		{
			dirty = true;
			playerIconsDirty = true;
			applyOverrides();
			refreshPlayerIcons();
		});
		refreshPanel();
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		navButton = null;
		panel = null;
		clientThread.invokeLater(this::clearOverrides);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Rank titles can change while logged in (e.g. a clan admin renames a rank), so recheck now and then.
		if (++tickCounter >= RECHECK_TICKS)
		{
			tickCounter = 0;
			refreshPlayerIcons();
			if (applyOverrides())
			{
				refreshPanel();
			}
		}
	}

	@Subscribe
	public void onClanChannelChanged(ClanChannelChanged event)
	{
		if (!event.isGuest())
		{
			applyOverrides();
			refreshPanel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// RuneLite rebuilds its chat icons when the client restarts, so put ours back
			dirty = true;
			playerIconsDirty = true;
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"chatMessageBuilding".equals(event.getEventName()))
		{
			return;
		}

		int uid = client.getIntStack()[client.getIntStackSize() - 1];
		MessageNode messageNode = client.getMessages().get(uid);
		if (messageNode == null)
		{
			return;
		}

		ChatMessageType type = messageNode.getType();
		boolean publicChat = type == ChatMessageType.PUBLICCHAT || type == ChatMessageType.MODCHAT;
		boolean playerMessage = publicChat
			|| type == ChatMessageType.CLAN_CHAT
			|| type == ChatMessageType.CLAN_GUEST_CHAT
			|| type == ChatMessageType.FRIENDSCHAT
			|| type == ChatMessageType.PRIVATECHAT
			|| type == ChatMessageType.MODPRIVATECHAT;
		if (!playerMessage)
		{
			return;
		}

		Object[] objectStack = client.getObjectStack();
		int objectStackSize = client.getObjectStackSize();
		String name = (String) objectStack[objectStackSize - 3];

		// A player's own icon wins over their rank icon
		Integer playerIcon = playerChatIcons.get(PlayerIcons.normalizeName(name));
		int iconNumber = playerIcon == null ? -1 : chatIconManager.chatIconIndex(playerIcon);
		if (iconNumber < 0 && publicChat && config.showIconsInPublicChat())
		{
			iconNumber = clanIconNumber(name);
		}

		if (iconNumber > -1)
		{
			objectStack[objectStackSize - 3] = "<img=" + iconNumber + ">" + name;
		}
	}

	private int clanIconNumber(String name)
	{
		ClanSettings settings = client.getClanSettings();
		if (settings == null)
		{
			return -1;
		}

		ClanMember member = settings.findMember(Text.toJagexName(Text.removeTags(name)));
		if (member == null || member.getRank() == null)
		{
			return -1;
		}

		ClanTitle title = settings.titleForRank(member.getRank());
		return title == null ? -1 : chatIconManager.getIconNumber(title);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!ClanRankIconsConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if ("showSidebarButton".equals(event.getKey()))
		{
			if (config.showSidebarButton())
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				clientToolbar.removeNavigation(navButton);
			}
			return;
		}

		clientThread.invokeLater(() ->
		{
			if (PLAYER_ICONS_KEY.equals(event.getKey()))
			{
				playerIconsDirty = true;
				refreshPlayerIcons();
			}
			else
			{
				dirty = true;
				applyOverrides();
			}
		});
		refreshPanel();
	}

	Map<String, IconChoice> getPlayerIcons()
	{
		return PlayerIcons.parse(configManager.getConfiguration(ClanRankIconsConfig.GROUP, PLAYER_ICONS_KEY));
	}

	void setPlayerIcon(String name, @Nullable IconChoice choice)
	{
		String key = PlayerIcons.normalizeName(name);
		if (key.isEmpty())
		{
			return;
		}

		Map<String, IconChoice> icons = getPlayerIcons();
		if (choice == null)
		{
			icons.remove(key);
		}
		else
		{
			icons.put(key, choice);
		}

		if (icons.isEmpty())
		{
			configManager.unsetConfiguration(ClanRankIconsConfig.GROUP, PLAYER_ICONS_KEY);
		}
		else
		{
			configManager.setConfiguration(ClanRankIconsConfig.GROUP, PLAYER_ICONS_KEY, PlayerIcons.serialize(icons));
		}
	}

	/**
	 * Builds a chat icon for every player with their own icon. Must be called on the client thread.
	 */
	private void refreshPlayerIcons()
	{
		if (!playerIconsDirty)
		{
			return;
		}

		spareChatIcons.addAll(playerChatIcons.values());
		playerChatIcons.clear();
		for (Map.Entry<String, IconChoice> entry : getPlayerIcons().entrySet())
		{
			if (entry.getValue().getType() == IconChoice.Type.HIDDEN)
			{
				continue;
			}

			BufferedImage image = renderChoice(entry.getValue(), CHAT_ICON_SIZE, CHAT_ICON_SIZE);
			if (image == null)
			{
				// Sprites aren't loaded yet, try again later
				return;
			}

			int iconId;
			if (spareChatIcons.isEmpty())
			{
				iconId = chatIconManager.registerChatIcon(image);
			}
			else
			{
				iconId = spareChatIcons.remove(spareChatIcons.size() - 1);
				chatIconManager.updateChatIcon(iconId, image);
			}
			playerChatIcons.put(entry.getKey(), iconId);
		}
		playerIconsDirty = false;
	}

	/**
	 * Gives the current clan's ranks a themed set of in-game icons, lowest rank to highest. Guests are left alone.
	 */
	void applyPreset(String presetName)
	{
		List<String> preset = Presets.ALL.get(presetName);
		if (preset == null)
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			EnumComposition rankGraphics = client.getEnum(EnumID.CLAN_RANK_GRAPHIC);
			EnumComposition rankNames = client.getEnum(EnumID.CLAN_RANK_NAME);
			Map<String, Integer> spritesByName = new HashMap<>();
			for (int key : rankNames.getKeys())
			{
				spritesByName.putIfAbsent(Presets.normalize(rankNames.getStringValue(key)), rankGraphics.getIntValue(key));
			}

			List<Integer> sprites = new ArrayList<>();
			for (String name : preset)
			{
				Integer spriteId = spritesByName.get(Presets.normalize(name));
				if (spriteId != null && spriteId >= 0)
				{
					sprites.add(spriteId);
				}
			}

			List<Integer> ranks = new ArrayList<>();
			for (RankRow row : buildRows())
			{
				if (row.getRank() != ClanRank.GUEST.getRank())
				{
					ranks.add(row.getRank());
				}
			}
			Collections.sort(ranks);
			if (sprites.isEmpty() || ranks.isEmpty())
			{
				return;
			}

			for (int i = 0; i < ranks.size(); i++)
			{
				setChoice(ranks.get(i), IconChoice.sprite(sprites.get(Presets.iconIndexFor(i, ranks.size(), sprites.size()))));
			}
		});
	}

	@Nullable
	private IconChoice getChoice(int rank)
	{
		String clan = clanKey;
		if (clan == null)
		{
			return null;
		}
		return IconChoice.parse(configManager.getConfiguration(ClanRankIconsConfig.GROUP, rankKey(clan, rank)));
	}

	void setChoice(int rank, IconChoice choice)
	{
		String clan = clanKey;
		if (clan != null)
		{
			configManager.setConfiguration(ClanRankIconsConfig.GROUP, rankKey(clan, rank), choice.serialize());
		}
	}

	void resetChoice(int rank)
	{
		String clan = clanKey;
		if (clan != null)
		{
			configManager.unsetConfiguration(ClanRankIconsConfig.GROUP, rankKey(clan, rank));
		}
	}

	/**
	 * @return a share code for the current clan's in-game and hidden icons, or null when not in a clan
	 */
	@Nullable
	String exportShareCode()
	{
		if (clanKey == null)
		{
			return null;
		}

		Map<Integer, IconChoice> choices = new LinkedHashMap<>();
		for (int rank = MAX_RANK; rank >= MIN_RANK; rank--)
		{
			IconChoice choice = getChoice(rank);
			if (choice != null)
			{
				choices.put(rank, choice);
			}
		}
		return ShareCode.encode(choices);
	}

	/**
	 * Applies a share code to the current clan.
	 *
	 * @return how many ranks were changed, or -1 when not in a clan
	 * @throws IllegalArgumentException if the code isn't valid
	 */
	int importShareCode(String code)
	{
		Map<Integer, IconChoice> choices = ShareCode.decode(code);
		if (clanKey == null)
		{
			return -1;
		}

		for (Map.Entry<Integer, IconChoice> entry : choices.entrySet())
		{
			setChoice(entry.getKey(), entry.getValue());
		}
		return choices.size();
	}

	private static String rankKey(String clan, int rank)
	{
		return "clan_" + clan + "_rank_" + rank;
	}

	/**
	 * Remembers which clan we're in, and moves any pre-1.1 choices over to it. Must be called on the client thread.
	 */
	private void updateClanKey(ClanSettings settings)
	{
		String name = settings.getName();
		String key = name == null ? null : name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
		if (key == null || key.equals(clanKey))
		{
			return;
		}

		clanKey = key;
		for (int rank = MIN_RANK; rank <= MAX_RANK; rank++)
		{
			String legacy = configManager.getConfiguration(ClanRankIconsConfig.GROUP, LEGACY_RANK_KEY_PREFIX + rank);
			if (legacy == null)
			{
				continue;
			}

			if (configManager.getConfiguration(ClanRankIconsConfig.GROUP, rankKey(key, rank)) == null)
			{
				configManager.setConfiguration(ClanRankIconsConfig.GROUP, rankKey(key, rank), legacy);
			}
			configManager.unsetConfiguration(ClanRankIconsConfig.GROUP, LEGACY_RANK_KEY_PREFIX + rank);
		}
	}

	/**
	 * Works out which sprite each configured rank uses right now and swaps it for the chosen icon.
	 * Must be called on the client thread.
	 *
	 * @return true if the applied icons changed
	 */
	private boolean applyOverrides()
	{
		ClanSettings settings = client.getClanSettings();
		Map<ClanTitle, IconChoice> plan = new LinkedHashMap<>();
		if (settings != null)
		{
			updateClanKey(settings);

			// Highest rank first, so if two ranks share a title the more senior rank's choice wins
			for (int rank = MAX_RANK; rank >= MIN_RANK; rank--)
			{
				IconChoice choice = getChoice(rank);
				if (choice == null)
				{
					continue;
				}

				ClanTitle title = settings.titleForRank(new ClanRank(rank));
				if (title != null)
				{
					plan.putIfAbsent(title, choice);
				}
			}
		}

		if (!dirty && plan.equals(appliedPlan))
		{
			return false;
		}

		clearOverrides();

		EnumComposition rankGraphics = client.getEnum(EnumID.CLAN_RANK_GRAPHIC);
		Set<Integer> applied = new HashSet<>();
		for (Map.Entry<ClanTitle, IconChoice> entry : plan.entrySet())
		{
			ClanTitle title = entry.getKey();
			int spriteId = rankGraphics.getIntValue(title.getId());
			if (spriteId < 0 || applied.contains(spriteId))
			{
				continue;
			}

			BufferedImage original = spriteManager.getSprite(spriteId, 0);
			if (original == null)
			{
				// Game cache isn't ready yet, try again on a later tick
				dirty = true;
				return false;
			}

			BufferedImage replacement = renderChoice(entry.getValue(), original.getWidth(), original.getHeight());
			if (replacement == null)
			{
				continue;
			}

			client.getSpriteOverrides().put(spriteId, ImageUtil.getImageSpritePixels(replacement, client));
			overriddenSprites.add(spriteId);
			applied.add(spriteId);

			if (config.replaceChatChannelIcons())
			{
				replaceChatIcon(title, entry.getValue());
			}
		}

		client.getWidgetSpriteCache().reset();
		appliedPlan = plan;
		dirty = false;
		return true;
	}

	private void replaceChatIcon(ClanTitle title, IconChoice choice)
	{
		int index = chatIconManager.getIconNumber(title);
		IndexedSprite[] modIcons = client.getModIcons();
		if (index < 0 || modIcons == null || index >= modIcons.length)
		{
			return;
		}

		BufferedImage image = renderChoice(choice, CHAT_ICON_SIZE, CHAT_ICON_SIZE);
		if (image != null)
		{
			modIcons[index] = ImageUtil.getImageIndexedSprite(image, client);
			overriddenChatIcons.add(title);
		}
	}

	/**
	 * Puts every icon we replaced back to the game's original. Must be called on the client thread.
	 */
	private void clearOverrides()
	{
		Map<Integer, ?> overrides = client.getSpriteOverrides();
		for (int spriteId : overriddenSprites)
		{
			overrides.remove(spriteId);
		}
		overriddenSprites.clear();

		IndexedSprite[] modIcons = client.getModIcons();
		for (ClanTitle title : overriddenChatIcons)
		{
			int index = chatIconManager.getIconNumber(title);
			BufferedImage original = chatIconManager.getRankImage(title);
			if (modIcons != null && index >= 0 && index < modIcons.length && original != null)
			{
				modIcons[index] = ImageUtil.getImageIndexedSprite(original, client);
			}
		}
		overriddenChatIcons.clear();

		client.getWidgetSpriteCache().reset();
		appliedPlan = Collections.emptyMap();
	}

	/**
	 * Draws the chosen icon at the given size. Must be called on the client thread.
	 */
	@Nullable
	private BufferedImage renderChoice(IconChoice choice, int width, int height)
	{
		if (choice.getType() == IconChoice.Type.HIDDEN)
		{
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		BufferedImage source = spriteManager.getSprite(choice.getSpriteId(), 0);
		if (source == null)
		{
			return null;
		}
		return ImageScaling.hardenAlpha(ImageScaling.fit(source, width, height, true, true));
	}

	/**
	 * Rebuilds the side panel from the player's current clan.
	 */
	void refreshPanel()
	{
		clientThread.invokeLater(() ->
		{
			ClanRankIconsPanel currentPanel = panel;
			if (currentPanel == null)
			{
				return;
			}

			List<RankRow> rows = buildRows();
			List<PlayerRow> players = new ArrayList<>();
			for (Map.Entry<String, IconChoice> entry : getPlayerIcons().entrySet())
			{
				players.add(new PlayerRow(entry.getKey(), renderChoice(entry.getValue(), CHAT_ICON_SIZE, CHAT_ICON_SIZE)));
			}
			boolean inClan = client.getClanSettings() != null;
			SwingUtilities.invokeLater(() -> currentPanel.showRanks(rows, players, inClan));
		});
	}

	private List<RankRow> buildRows()
	{
		List<RankRow> rows = new ArrayList<>();
		ClanSettings settings = client.getClanSettings();
		if (settings == null)
		{
			return rows;
		}
		updateClanKey(settings);

		// Ranks that someone in the clan holds, plus the fixed senior ranks, guests, and anything already customised
		Set<Integer> ranks = new TreeSet<>(Collections.reverseOrder());
		ranks.add(ClanRank.OWNER.getRank());
		ranks.add(ClanRank.DEPUTY_OWNER.getRank());
		ranks.add(ClanRank.ADMINISTRATOR.getRank());
		ranks.add(ClanRank.GUEST.getRank());
		for (ClanMember member : settings.getMembers())
		{
			if (member.getRank() != null)
			{
				ranks.add(member.getRank().getRank());
			}
		}
		for (int rank = MIN_RANK; rank <= MAX_RANK; rank++)
		{
			if (getChoice(rank) != null)
			{
				ranks.add(rank);
			}
		}

		EnumComposition rankGraphics = client.getEnum(EnumID.CLAN_RANK_GRAPHIC);
		for (int rank : ranks)
		{
			ClanTitle title = settings.titleForRank(new ClanRank(rank));
			if (title == null)
			{
				continue;
			}

			int spriteId = rankGraphics.getIntValue(title.getId());
			BufferedImage original = spriteId >= 0 ? spriteManager.getSprite(spriteId, 0) : null;
			IconChoice choice = getChoice(rank);
			BufferedImage custom = null;
			if (choice != null)
			{
				int width = original != null ? original.getWidth() : CHAT_ICON_SIZE;
				int height = original != null ? original.getHeight() : CHAT_ICON_SIZE;
				custom = renderChoice(choice, width, height);
			}
			rows.add(new RankRow(rank, title.getName(), original, custom, choice != null && choice.getType() == IconChoice.Type.HIDDEN));
		}
		return rows;
	}

	/**
	 * Collects every clan rank icon in the game, for the icon picker, then hands them to the callback on the Swing thread.
	 */
	void loadGameIcons(Consumer<List<GameIcon>> callback)
	{
		clientThread.invokeLater(() ->
		{
			EnumComposition rankGraphics = client.getEnum(EnumID.CLAN_RANK_GRAPHIC);
			EnumComposition rankNames = client.getEnum(EnumID.CLAN_RANK_NAME);
			Map<Integer, GameIcon> icons = new LinkedHashMap<>();
			for (int key : rankGraphics.getKeys())
			{
				int spriteId = rankGraphics.getIntValue(key);
				if (spriteId < 0 || icons.containsKey(spriteId))
				{
					continue;
				}

				BufferedImage image = spriteManager.getSprite(spriteId, 0);
				if (image != null)
				{
					icons.put(spriteId, new GameIcon(spriteId, rankNames.getStringValue(key), image));
				}
			}

			List<GameIcon> list = new ArrayList<>(icons.values());
			SwingUtilities.invokeLater(() -> callback.accept(list));
		});
	}
}
