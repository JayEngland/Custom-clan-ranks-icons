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
 * The icon a player picked for a rank: either another game sprite, or an image they uploaded.
 * Stored in the RuneLite config as "sprite:&lt;id&gt;" or "image:&lt;base64 png&gt;".
 */
@Value
class IconChoice
{
	private static final String SPRITE_PREFIX = "sprite:";
	private static final String IMAGE_PREFIX = "image:";

	/**
	 * Uploaded images are shrunk to this size before saving, so the config stays small.
	 */
	private static final int MAX_STORED_SIZE = 32;

	enum Type
	{
		SPRITE,
		IMAGE
	}

	Type type;
	int spriteId;
	String imageData;

	static IconChoice sprite(int spriteId)
	{
		return new IconChoice(Type.SPRITE, spriteId, null);
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
		return type == Type.SPRITE ? SPRITE_PREFIX + spriteId : IMAGE_PREFIX + imageData;
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
