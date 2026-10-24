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

import javax.annotation.Nullable;
import lombok.Value;

/**
 * The icon a player picked for a rank: another game sprite, or no icon at all.
 * Stored in the RuneLite config as "sprite:&lt;id&gt;" or "hidden".
 */
@Value
class IconChoice
{
	private static final String SPRITE_PREFIX = "sprite:";
	private static final String HIDDEN = "hidden";

	enum Type
	{
		SPRITE,
		HIDDEN
	}

	Type type;
	int spriteId;

	static IconChoice sprite(int spriteId)
	{
		return new IconChoice(Type.SPRITE, spriteId);
	}

	static IconChoice hidden()
	{
		return new IconChoice(Type.HIDDEN, -1);
	}

	/**
	 * @return the choice, or null for anything else (including images uploaded with early test builds)
	 */
	@Nullable
	static IconChoice parse(@Nullable String value)
	{
		if (value == null)
		{
			return null;
		}

		try
		{
			if (value.startsWith(SPRITE_PREFIX))
			{
				return sprite(Integer.parseInt(value.substring(SPRITE_PREFIX.length())));
			}
			if (value.equals(HIDDEN))
			{
				return hidden();
			}
		}
		catch (NumberFormatException ignored)
		{
		}
		return null;
	}

	String serialize()
	{
		return type == Type.SPRITE ? SPRITE_PREFIX + spriteId : HIDDEN;
	}
}
