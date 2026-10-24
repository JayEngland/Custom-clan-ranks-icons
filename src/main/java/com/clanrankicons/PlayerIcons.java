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

import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.client.util.Text;

/**
 * Icons given to individual players, stored in one config value as "name=choice" lines.
 */
final class PlayerIcons
{
	private PlayerIcons()
	{
	}

	static String normalizeName(String name)
	{
		return name == null ? "" : Text.toJagexName(Text.removeTags(name)).toLowerCase();
	}

	static Map<String, IconChoice> parse(String value)
	{
		Map<String, IconChoice> icons = new LinkedHashMap<>();
		if (value == null)
		{
			return icons;
		}

		for (String line : value.split("\n"))
		{
			int split = line.indexOf('=');
			if (split <= 0)
			{
				continue;
			}

			IconChoice choice = IconChoice.parse(line.substring(split + 1));
			if (choice != null)
			{
				icons.put(line.substring(0, split), choice);
			}
		}
		return icons;
	}

	static String serialize(Map<String, IconChoice> icons)
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, IconChoice> entry : icons.entrySet())
		{
			if (sb.length() > 0)
			{
				sb.append('\n');
			}
			sb.append(entry.getKey()).append('=').append(entry.getValue().serialize());
		}
		return sb.toString();
	}
}
