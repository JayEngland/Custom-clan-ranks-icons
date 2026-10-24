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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class ImageScaling
{
	private ImageScaling()
	{
	}

	/**
	 * Scales an image so it fits in a width x height box, centred on a transparent canvas of exactly that size
	 * (or of the scaled size when {@code padToBox} is false).
	 */
	static BufferedImage fit(BufferedImage image, int width, int height, boolean keepAspectRatio, boolean padToBox)
	{
		int newWidth = width;
		int newHeight = height;
		if (keepAspectRatio)
		{
			double scale = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
			newWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
			newHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
		}

		int canvasWidth = padToBox ? width : newWidth;
		int canvasHeight = padToBox ? height : newHeight;
		BufferedImage out = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(image, (canvasWidth - newWidth) / 2, (canvasHeight - newHeight) / 2, newWidth, newHeight, null);
		g.dispose();
		return out;
	}

	/**
	 * The game draws sprite pixels either fully opaque or not at all, so snap
	 * half-transparent edge pixels one way or the other to avoid dark halos.
	 */
	static BufferedImage hardenAlpha(BufferedImage image)
	{
		BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int argb = image.getRGB(x, y);
				int alpha = argb >>> 24;
				out.setRGB(x, y, alpha < 128 ? 0 : (argb | 0xFF000000));
			}
		}
		return out;
	}
}
