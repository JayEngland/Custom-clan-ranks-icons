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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
