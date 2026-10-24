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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import lombok.Value;

/**
 * The icon a player picked for a rank: another game sprite, an image they uploaded, or no icon at all.
 * Stored in the RuneLite config as "sprite:&lt;id&gt;", "image:&lt;base64 png&gt;" or "hidden".
 */
@Value
class IconChoice
{
	private static final String SPRITE_PREFIX = "sprite:";
	private static final String IMAGE_PREFIX = "image:";
	private static final String HIDDEN = "hidden";

	/**
	 * Uploaded images are shrunk to this size before saving, so the config stays small.
	 */
	private static final int MAX_STORED_SIZE = 32;

	enum Type
	{
		SPRITE,
		IMAGE,
		HIDDEN
	}

	Type type;
	int spriteId;
	String imageData;

	static IconChoice sprite(int spriteId)
	{
		return new IconChoice(Type.SPRITE, spriteId, null);
	}

	static IconChoice hidden()
	{
		return new IconChoice(Type.HIDDEN, -1, null);
	}

	static IconChoice image(BufferedImage image) throws IOException
	{
		BufferedImage scaled = ImageScaling.fit(image, MAX_STORED_SIZE, MAX_STORED_SIZE, true, false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(scaled, "png", out);
		return new IconChoice(Type.IMAGE, -1, Base64.getEncoder().encodeToString(out.toByteArray()));
	}

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
			if (value.startsWith(IMAGE_PREFIX))
			{
				return new IconChoice(Type.IMAGE, -1, value.substring(IMAGE_PREFIX.length()));
			}
		}
		catch (NumberFormatException ignored)
		{
		}
		return null;
	}

	String serialize()
	{
		switch (type)
		{
			case SPRITE:
				return SPRITE_PREFIX + spriteId;
			case HIDDEN:
				return HIDDEN;
			default:
				return IMAGE_PREFIX + imageData;
		}
	}

	@Nullable
	BufferedImage decodeImage()
	{
		if (type != Type.IMAGE)
		{
			return null;
		}

		try
		{
			return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageData)));
		}
		catch (IOException | IllegalArgumentException e)
		{
			return null;
		}
	}
}
