package com.clanrankicons;

import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import lombok.Value;

/**
 * One rank in the player's clan, as shown in the side panel.
 */
@Value
class RankRow
{
	int rank;
	String title;
	@Nullable
	BufferedImage originalIcon;
	@Nullable
	BufferedImage customIcon;
}
