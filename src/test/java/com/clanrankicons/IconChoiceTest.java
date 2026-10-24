package com.clanrankicons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.awt.image.BufferedImage;
import org.junit.Test;

public class IconChoiceTest
{
	@Test
	public void spriteChoiceRoundTrips()
	{
		IconChoice choice = IconChoice.sprite(1234);
		assertEquals(choice, IconChoice.parse(choice.serialize()));
	}

	@Test
	public void uploadedImageIsShrunkAndRoundTrips() throws Exception
	{
		IconChoice choice = IconChoice.image(new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB));
		IconChoice parsed = IconChoice.parse(choice.serialize());
		assertEquals(choice, parsed);

		BufferedImage decoded = parsed.decodeImage();
		assertNotNull(decoded);
		assertEquals(32, decoded.getWidth());
		assertEquals(16, decoded.getHeight());
	}

	@Test
	public void badValuesAreIgnored()
	{
		assertNull(IconChoice.parse(null));
		assertNull(IconChoice.parse("sprite:abc"));
		assertNull(IconChoice.parse("something else"));
	}

	@Test
	public void fitCentresImageInBox()
	{
		BufferedImage fitted = ImageScaling.fit(new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB), 11, 11, true, true);
		assertEquals(11, fitted.getWidth());
		assertEquals(11, fitted.getHeight());
	}
}
