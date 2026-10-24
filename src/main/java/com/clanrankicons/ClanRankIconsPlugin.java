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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
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

@Slf4j
@PluginDescriptor(
	name = "Custom Clan Rank Icons",
	description = "Pick your own icon for each rank in your clan. Only you can see the changes.",
	tags = {"clan", "rank", "icon", "custom", "chat"}
)
public class ClanRankIconsPlugin extends Plugin
{
	private static final String RANK_KEY_PREFIX = "rank_";
	private static final int MIN_RANK = -1;
	private static final int MAX_RANK = 127;
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
			applyOverrides();
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
		}
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
			dirty = true;
			applyOverrides();
		});
		refreshPanel();
	}

	@Nullable
	private IconChoice getChoice(int rank)
	{
		return IconChoice.parse(configManager.getConfiguration(ClanRankIconsConfig.GROUP, RANK_KEY_PREFIX + rank));
	}

	void setChoice(int rank, IconChoice choice)
	{
		configManager.setConfiguration(ClanRankIconsConfig.GROUP, RANK_KEY_PREFIX + rank, choice.serialize());
	}

	void setUploadedImage(int rank, BufferedImage image) throws IOException
	{
		setChoice(rank, IconChoice.image(image));
	}

	void resetChoice(int rank)
	{
		configManager.unsetConfiguration(ClanRankIconsConfig.GROUP, RANK_KEY_PREFIX + rank);
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
		BufferedImage source;
		boolean keepAspectRatio = true;
		if (choice.getType() == IconChoice.Type.SPRITE)
		{
			source = spriteManager.getSprite(choice.getSpriteId(), 0);
		}
		else
		{
			source = choice.decodeImage();
			keepAspectRatio = config.keepAspectRatio();
		}

		if (source == null)
		{
			return null;
		}

		return ImageScaling.hardenAlpha(ImageScaling.fit(source, width, height, keepAspectRatio, true));
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
			SwingUtilities.invokeLater(() -> currentPanel.showRanks(rows, client.getClanSettings() != null));
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
			rows.add(new RankRow(rank, title.getName(), original, custom));
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
