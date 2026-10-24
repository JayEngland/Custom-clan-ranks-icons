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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Themed sets of in-game clan rank icons, listed from lowest to highest.
 */
final class Presets
{
	static final Map<String, List<String>> ALL;

	static
	{
		Map<String, List<String>> presets = new LinkedHashMap<>();
		presets.put("Military", Arrays.asList("Recruit", "Corporal", "Sergeant", "Lieutenant", "Captain", "General"));
		presets.put("Metals", Arrays.asList("Bronze", "Iron", "Steel", "Mithril", "Adamant", "Rune", "Dragon"));
		presets.put("Gems", Arrays.asList("Opal", "Jade", "Red Topaz", "Sapphire", "Emerald", "Ruby", "Diamond", "Dragonstone", "Onyx", "Zenyte"));
		presets.put("Royal court", Arrays.asList("Pawn", "Page", "Squire", "Knight", "Noble", "Monarch"));
		presets.put("Gods", Arrays.asList("Serenist", "Xerician", "Tirannian", "Armadylean", "Bandosian", "Guthixian", "Zarosian", "Zamorakian", "Saradominist"));
		ALL = Collections.unmodifiableMap(presets);
	}

	private Presets()
	{
	}

	/**
	 * Picks which preset icon each rank gets. Ranks are ordered lowest to highest; the highest rank always gets
	 * the last icon and the rest are spread evenly below it.
	 */
	static int iconIndexFor(int position, int rankCount, int iconCount)
	{
		if (rankCount <= 1)
		{
			return iconCount - 1;
		}
		return (int) Math.round((double) position * (iconCount - 1) / (rankCount - 1));
	}

	static String normalize(String name)
	{
		return name == null ? "" : name.toLowerCase().replaceAll("[^a-z]", "");
	}
}
