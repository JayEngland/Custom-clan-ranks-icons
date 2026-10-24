package com.clanrankicons;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ClanRankIconsConfig.GROUP)
public interface ClanRankIconsConfig extends Config
{
	String GROUP = "customclanrankicons";

	@ConfigItem(
		keyName = "keepAspectRatio",
		name = "Keep aspect ratio",
		description = "Keep the shape of uploaded images when fitting them into the rank icon's space, instead of stretching them",
		position = 1
	)
	default boolean keepAspectRatio()
	{
		return true;
	}

	@ConfigItem(
		keyName = "replaceChatChannelIcons",
		name = "Replace join/leave icons",
		description = "Also replace the rank icons shown in clan join/leave messages added by RuneLite's Chat Channel plugin",
		position = 2
	)
	default boolean replaceChatChannelIcons()
	{
		return true;
	}
}
