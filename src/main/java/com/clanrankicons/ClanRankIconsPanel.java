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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Slf4j
class ClanRankIconsPanel extends PluginPanel
{
	private static final int ICON_PREVIEW_SIZE = 20;
	private static final int PICKER_COLUMNS = 6;

	private final ClanRankIconsPlugin plugin;
	private final JPanel rankList = new JPanel();
	private final JLabel status = new JLabel();

	ClanRankIconsPanel(ClanRankIconsPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel header = new JPanel(new BorderLayout(0, 4));
		JLabel title = new JLabel("Clan Rank Icons");
		title.setFont(FontManager.getRunescapeBoldFont());
		header.add(title, BorderLayout.NORTH);
		JLabel help = new JLabel("<html>Choose a new icon for any rank in your clan. Only you will see it.</html>");
		help.setFont(FontManager.getRunescapeSmallFont());
		help.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		header.add(help, BorderLayout.CENTER);
		JButton refresh = new JButton("Refresh ranks");
		refresh.addActionListener(e -> plugin.refreshPanel());
		header.add(refresh, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		rankList.setLayout(new BoxLayout(rankList, BoxLayout.Y_AXIS));
		JPanel listWrapper = new JPanel(new BorderLayout());
		listWrapper.add(status, BorderLayout.NORTH);
		listWrapper.add(rankList, BorderLayout.CENTER);
		add(listWrapper, BorderLayout.CENTER);

		status.setFont(FontManager.getRunescapeSmallFont());
		status.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		status.setText("<html>Log in and join your clan to see its ranks.</html>");
	}

	void showRanks(List<RankRow> rows, boolean inClan)
	{
		rankList.removeAll();
		if (!inClan)
		{
			status.setText("<html>Log in and join your clan to see its ranks.</html>");
			status.setVisible(true);
		}
		else if (rows.isEmpty())
		{
			status.setText("<html>No ranks found yet. Try Refresh ranks in a moment.</html>");
			status.setVisible(true);
		}
		else
		{
			status.setVisible(false);
		}

		for (RankRow row : rows)
		{
			rankList.add(createRow(row));
		}
		rankList.revalidate();
		rankList.repaint();
	}

	private JPanel createRow(RankRow row)
	{
		JPanel panel = new JPanel(new BorderLayout(6, 4));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)));

		JPanel icons = new JPanel(new GridLayout(1, 2, 2, 0));
		icons.setOpaque(false);
		JLabel original = iconLabel(row.getOriginalIcon(), "Original icon");
		JLabel custom = iconLabel(row.getCustomIcon(), "Your icon");
		icons.add(original);
		icons.add(custom);
		panel.add(icons, BorderLayout.WEST);

		JLabel name = new JLabel("<html>" + escape(row.getTitle()) + "<br><font color='#a5a5a5'>Rank " + row.getRank() + "</font></html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		panel.add(name, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
		buttons.setOpaque(false);
		JButton gameIcon = smallButton("Game icon", "Pick another clan rank icon from the game");
		gameIcon.addActionListener(e -> openGameIconPicker(row));
		JButton upload = smallButton("Upload", "Use an image file from your computer (PNG, JPG or GIF)");
		upload.addActionListener(e -> uploadImage(row));
		JButton reset = smallButton("Reset", "Go back to the normal icon");
		reset.setEnabled(row.getCustomIcon() != null);
		reset.addActionListener(e -> plugin.resetChoice(row.getRank()));
		buttons.add(gameIcon);
		buttons.add(upload);
		buttons.add(reset);
		panel.add(buttons, BorderLayout.SOUTH);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private static JLabel iconLabel(BufferedImage image, String tooltip)
	{
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(ICON_PREVIEW_SIZE, ICON_PREVIEW_SIZE));
		label.setToolTipText(tooltip);
		if (image != null)
		{
			label.setIcon(new ImageIcon(image));
		}
		else
		{
			label.setText("-");
		}
		return label;
	}

	private static JButton smallButton(String text, String tooltip)
	{
		JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setToolTipText(tooltip);
		button.setMargin(new Insets(2, 2, 2, 2));
		return button;
	}

	private void openGameIconPicker(RankRow row)
	{
		plugin.loadGameIcons(icons ->
		{
			JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Icon for " + row.getTitle());
			dialog.setModal(true);

			JPanel grid = new JPanel(new GridLayout(0, PICKER_COLUMNS, 2, 2));
			grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
			for (GameIcon icon : icons)
			{
				JButton button = new JButton(new ImageIcon(icon.getImage()));
				button.setToolTipText(icon.getName());
				button.setPreferredSize(new Dimension(32, 32));
				button.addActionListener(e ->
				{
					plugin.setChoice(row.getRank(), IconChoice.sprite(icon.getSpriteId()));
					dialog.dispose();
				});
				grid.add(button);
			}

			JScrollPane scroll = new JScrollPane(grid);
			scroll.setPreferredSize(new Dimension(260, 320));
			scroll.getVerticalScrollBar().setUnitIncrement(16);
			dialog.add(scroll);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		});
	}

	private void uploadImage(RankRow row)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Icon for " + row.getTitle());
		chooser.setFileFilter(new FileNameExtensionFilter("Images (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}

		File file = chooser.getSelectedFile();
		try
		{
			BufferedImage image = ImageIO.read(file);
			if (image == null)
			{
				JOptionPane.showMessageDialog(this, "That file isn't an image RuneLite can read.", "Clan Rank Icons", JOptionPane.WARNING_MESSAGE);
				return;
			}
			plugin.setUploadedImage(row.getRank(), image);
		}
		catch (IOException ex)
		{
			log.warn("Unable to load icon image {}", file, ex);
			JOptionPane.showMessageDialog(this, "Couldn't read that image file.", "Clan Rank Icons", JOptionPane.WARNING_MESSAGE);
		}
	}

	private static String escape(String text)
	{
		return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
