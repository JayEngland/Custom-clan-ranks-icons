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
