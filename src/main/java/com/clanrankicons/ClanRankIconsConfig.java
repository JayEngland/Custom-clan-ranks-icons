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

	@ConfigItem(
		keyName = "showSidebarButton",
		name = "Show sidebar button",
		description = "Show the Clan Rank Icons button in the RuneLite sidebar. Your icons stay applied while it's hidden",
		position = 0
	)
	default boolean showSidebarButton()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showIconsInPublicChat",
		name = "Show icons in public chat",
		description = "Show clan rank icons next to your clanmates' names in public chat",
		position = 3
	)
	default boolean showIconsInPublicChat()
	{
		return true;
	}
}
