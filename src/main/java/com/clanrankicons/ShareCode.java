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

import java.util.Map;
import java.util.TreeMap;

/**
 * Turns a clan's icon choices into a short text code that clanmates can paste into their own plugin.
 * Format: "CRI1:126=s1234,100=h"
 */
final class ShareCode
{
	private static final String PREFIX = "CRI1:";

	private ShareCode()
	{
	}

	static String encode(Map<Integer, IconChoice> choices)
	{
		StringBuilder sb = new StringBuilder(PREFIX);
		for (Map.Entry<Integer, IconChoice> entry : new TreeMap<>(choices).descendingMap().entrySet())
		{
			IconChoice choice = entry.getValue();
			String value = choice.getType() == IconChoice.Type.SPRITE ? "s" + choice.getSpriteId() : "h";

			if (sb.length() > PREFIX.length())
			{
				sb.append(',');
			}
			sb.append(entry.getKey()).append('=').append(value);
		}
		return sb.toString();
	}

	/**
	 * @throws IllegalArgumentException if the code isn't a valid share code
	 */
	static Map<Integer, IconChoice> decode(String code)
	{
		String trimmed = code == null ? "" : code.trim();
		if (!trimmed.startsWith(PREFIX))
		{
			throw new IllegalArgumentException("not a share code");
		}

		Map<Integer, IconChoice> choices = new TreeMap<>();
		String body = trimmed.substring(PREFIX.length());
		if (body.isEmpty())
		{
			return choices;
		}

		for (String part : body.split(","))
		{
			String[] pair = part.split("=", 2);
			if (pair.length != 2 || pair[1].isEmpty())
			{
				throw new IllegalArgumentException("bad entry " + part);
			}

			int rank;
			try
			{
				rank = Integer.parseInt(pair[0].trim());
				if (pair[1].equals("h"))
				{
					choices.put(rank, IconChoice.hidden());
				}
				else if (pair[1].startsWith("s"))
				{
					choices.put(rank, IconChoice.sprite(Integer.parseInt(pair[1].substring(1))));
				}
				else
				{
					throw new IllegalArgumentException("bad entry " + part);
				}
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("bad entry " + part, e);
			}

			if (rank < ClanRankIconsPlugin.MIN_RANK || rank > ClanRankIconsPlugin.MAX_RANK)
			{
				throw new IllegalArgumentException("bad rank " + rank);
			}
		}
		return choices;
	}
}
