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
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ShareCodeTest
{
	@Test
	public void roundTripsSpritesAndHiddenIcons()
	{
		Map<Integer, IconChoice> choices = new LinkedHashMap<>();
		choices.put(126, IconChoice.sprite(1234));
		choices.put(10, IconChoice.hidden());

		String code = ShareCode.encode(choices);
		assertEquals("CRI1:126=s1234,10=h", code);

		Map<Integer, IconChoice> decoded = ShareCode.decode(code);
		assertEquals(2, decoded.size());
		assertEquals(IconChoice.sprite(1234), decoded.get(126));
		assertEquals(IconChoice.hidden(), decoded.get(10));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsOtherText()
	{
		ShareCode.decode("hello");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsBadRanks()
	{
		ShareCode.decode("CRI1:500=s1");
	}

	@Test
	public void hiddenChoiceRoundTrips()
	{
		assertEquals(IconChoice.hidden(), IconChoice.parse(IconChoice.hidden().serialize()));
	}

	@Test
	public void presetSpreadsIconsFromLowestToHighest()
	{
		// 3 ranks over 7 icons: lowest gets the first, highest the last
		assertEquals(0, Presets.iconIndexFor(0, 3, 7));
		assertEquals(3, Presets.iconIndexFor(1, 3, 7));
		assertEquals(6, Presets.iconIndexFor(2, 3, 7));
		assertEquals(6, Presets.iconIndexFor(0, 1, 7));
		assertEquals("redtopaz", Presets.normalize("Red Topaz"));
	}

	@Test
	public void playerIconsRoundTrip()
	{
		Map<String, IconChoice> icons = new LinkedHashMap<>();
		icons.put("zezima", IconChoice.sprite(42));
		icons.put("lynx titan", IconChoice.sprite(7));
		assertEquals(icons, PlayerIcons.parse(PlayerIcons.serialize(icons)));
	}
}
