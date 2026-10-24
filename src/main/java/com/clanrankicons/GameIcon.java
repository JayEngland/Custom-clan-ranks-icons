package com.clanrankicons;

import java.awt.image.BufferedImage;
import lombok.Value;

/**
 * A clan rank icon from the game, offered in the icon picker.
 */
@Value
class GameIcon
{
	int spriteId;
	String name;
	BufferedImage image;
}
