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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

class ClanRankIconsPanel extends PluginPanel
{
	private static final int ICON_PREVIEW_SIZE = 20;
	private static final int PICKER_COLUMNS = 6;
	private static final String DIALOG_TITLE = "Clan Rank Icons";

	private final ClanRankIconsPlugin plugin;
	private final JPanel rankList = new JPanel();
	private final JPanel playerList = new JPanel();
	private final JLabel status = new JLabel();
	private List<RankRow> currentRows = new ArrayList<>();

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

		JPanel actions = new JPanel(new GridLayout(0, 1, 0, 4));
		JButton refresh = new JButton("Refresh ranks");
		refresh.addActionListener(e -> plugin.refreshPanel());
		actions.add(refresh);

		JButton presets = smallButton("Presets", "Give your ranks a themed set of icons in one click");
		presets.addActionListener(e -> showPresetMenu(presets));
		JButton bulk = smallButton("Bulk apply", "Use one icon for a range of ranks");
		bulk.addActionListener(e -> openBulkApply());
		actions.add(buttonPair(presets, bulk));

		JButton copyCode = smallButton("Copy code", "Copy your icons as a code clanmates can paste into this plugin");
		copyCode.addActionListener(e -> copyShareCode());
		JButton pasteCode = smallButton("Paste code", "Use icons from a clanmate's share code");
		pasteCode.addActionListener(e -> pasteShareCode());
		actions.add(buttonPair(copyCode, pasteCode));

		header.add(actions, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		rankList.setLayout(new BoxLayout(rankList, BoxLayout.Y_AXIS));
		playerList.setLayout(new BoxLayout(playerList, BoxLayout.Y_AXIS));

		JPanel players = new JPanel(new BorderLayout(0, 4));
		JLabel playersTitle = new JLabel("Player icons");
		playersTitle.setFont(FontManager.getRunescapeBoldFont());
		JButton addPlayer = new JButton("Add player icon");
		addPlayer.setToolTipText("Give one player their own icon next to their name in chat");
		addPlayer.addActionListener(e -> addPlayerIcon());
		players.add(playersTitle, BorderLayout.NORTH);
		players.add(playerList, BorderLayout.CENTER);
		players.add(addPlayer, BorderLayout.SOUTH);

		JPanel listWrapper = new JPanel(new BorderLayout(0, 8));
		listWrapper.add(status, BorderLayout.NORTH);
		listWrapper.add(rankList, BorderLayout.CENTER);
		listWrapper.add(players, BorderLayout.SOUTH);
		add(listWrapper, BorderLayout.CENTER);

		status.setFont(FontManager.getRunescapeSmallFont());
		status.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		status.setText("<html>Log in and join your clan to see its ranks.</html>");
	}

	void showRanks(List<RankRow> rows, List<PlayerRow> players, boolean inClan)
	{
		currentRows = rows;
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

		playerList.removeAll();
		for (PlayerRow player : players)
		{
			playerList.add(createPlayerRow(player));
		}
		playerList.revalidate();
		playerList.repaint();
	}

	private JPanel createRow(RankRow row)
	{
		JPanel panel = card();

		JPanel icons = new JPanel(new GridLayout(1, 2, 2, 0));
		icons.setOpaque(false);
		icons.add(iconLabel(row.getOriginalIcon(), "Original icon"));
		icons.add(row.isHidden() ? textLabel("off", "Icon hidden") : iconLabel(row.getCustomIcon(), "Your icon"));
		panel.add(icons, BorderLayout.WEST);

		JLabel name = new JLabel("<html>" + escape(row.getTitle()) + "<br><font color='#a5a5a5'>Rank " + row.getRank() + "</font></html>");
		name.setFont(FontManager.getRunescapeSmallFont());
		panel.add(name, BorderLayout.CENTER);

		JButton gameIcon = smallButton("Change", "Pick another clan rank icon from the game");
		gameIcon.addActionListener(e -> openGameIconPicker("Icon for " + row.getTitle(), true, choice -> plugin.setChoice(row.getRank(), choice)));
		JButton reset = smallButton("Reset", "Go back to the normal icon");
		reset.setEnabled(row.getCustomIcon() != null);
		reset.addActionListener(e -> plugin.resetChoice(row.getRank()));
		panel.add(buttonPair(gameIcon, reset), BorderLayout.SOUTH);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private JPanel createPlayerRow(PlayerRow player)
	{
		JPanel panel = card();
		panel.add(iconLabel(player.getIcon(), "Their icon"), BorderLayout.WEST);

		JLabel name = new JLabel(player.getName());
		name.setFont(FontManager.getRunescapeSmallFont());
		panel.add(name, BorderLayout.CENTER);

		JButton remove = smallButton("Remove", "Stop showing an icon for this player");
		remove.addActionListener(e -> plugin.setPlayerIcon(player.getName(), null));
		panel.add(remove, BorderLayout.EAST);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private static JPanel card()
	{
		JPanel panel = new JPanel(new BorderLayout(6, 4));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		return panel;
	}

	private static JPanel buttonPair(JButton left, JButton right)
	{
		JPanel pair = new JPanel(new GridLayout(1, 2, 4, 0));
		pair.setOpaque(false);
		pair.add(left);
		pair.add(right);
		return pair;
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

	private static JLabel textLabel(String text, String tooltip)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(ICON_PREVIEW_SIZE, ICON_PREVIEW_SIZE));
		label.setToolTipText(tooltip);
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

	/**
	 * Shows a searchable grid of every clan rank icon in the game, and passes the one picked to {@code onPick}.
	 */
	private void openGameIconPicker(String title, boolean allowHidden, Consumer<IconChoice> onPick)
	{
		plugin.loadGameIcons(icons ->
		{
			JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title);
			dialog.setModal(true);

			JPanel grid = new JPanel(new GridLayout(0, PICKER_COLUMNS, 2, 2));
			grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

			JTextField search = new JTextField();
			search.setToolTipText("Search icons by name");
			Runnable fillGrid = () ->
			{
				String query = search.getText().trim().toLowerCase();
				grid.removeAll();
				for (GameIcon icon : icons)
				{
					if (!query.isEmpty() && (icon.getName() == null || !icon.getName().toLowerCase().contains(query)))
					{
						continue;
					}

					JButton button = new JButton(new ImageIcon(icon.getImage()));
					button.setToolTipText(icon.getName());
					button.setPreferredSize(new Dimension(32, 32));
					button.addActionListener(e ->
					{
						onPick.accept(IconChoice.sprite(icon.getSpriteId()));
						dialog.dispose();
					});
					grid.add(button);
				}
				grid.revalidate();
				grid.repaint();
			};
			search.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					fillGrid.run();
				}

				@Override
				public void removeUpdate(DocumentEvent e)
				{
					fillGrid.run();
				}

				@Override
				public void changedUpdate(DocumentEvent e)
				{
					fillGrid.run();
				}
			});
			fillGrid.run();

			JPanel top = new JPanel(new BorderLayout(6, 0));
			top.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
			top.add(new JLabel("Search:"), BorderLayout.WEST);
			top.add(search, BorderLayout.CENTER);
			if (allowHidden)
			{
				JButton noIcon = new JButton("No icon");
				noIcon.setToolTipText("Hide the icon completely");
				noIcon.addActionListener(e ->
				{
					onPick.accept(IconChoice.hidden());
					dialog.dispose();
				});
				top.add(noIcon, BorderLayout.EAST);
			}

			JScrollPane scroll = new JScrollPane(grid);
			scroll.setPreferredSize(new Dimension(280, 320));
			scroll.getVerticalScrollBar().setUnitIncrement(16);
			dialog.setLayout(new BorderLayout());
			dialog.add(top, BorderLayout.NORTH);
			dialog.add(scroll, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		});
	}

	private void showPresetMenu(JButton anchor)
	{
		JPopupMenu menu = new JPopupMenu();
		for (String preset : Presets.ALL.keySet())
		{
			JMenuItem item = new JMenuItem(preset + ": " + String.join(", ", Presets.ALL.get(preset)));
			item.addActionListener(e ->
			{
				if (currentRows.isEmpty())
				{
					JOptionPane.showMessageDialog(this, "Log in and join your clan first.", DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				int answer = JOptionPane.showConfirmDialog(this,
					"Give all your clan's ranks the " + preset + " icons, from lowest rank to highest?\n"
						+ "This replaces any icons you've already picked. Guests aren't changed.",
					DIALOG_TITLE, JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION)
				{
					plugin.applyPreset(preset);
				}
			});
			menu.add(item);
		}
		menu.show(anchor, 0, anchor.getHeight());
	}

	private void openBulkApply()
	{
		if (currentRows.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Log in and join your clan first.", DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		String[] labels = new String[currentRows.size()];
		for (int i = 0; i < labels.length; i++)
		{
			RankRow row = currentRows.get(i);
			labels[i] = row.getTitle() + " (rank " + row.getRank() + ")";
		}

		// Rows are listed highest rank first
		JComboBox<String> from = new JComboBox<>(labels);
		from.setSelectedIndex(labels.length - 1);
		JComboBox<String> to = new JComboBox<>(labels);
		to.setSelectedIndex(0);

		JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
		form.add(new JLabel("From rank:"));
		form.add(from);
		form.add(new JLabel("Up to and including:"));
		form.add(to);

		int answer = JOptionPane.showConfirmDialog(this, form, "Bulk apply", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION)
		{
			return;
		}

		int a = currentRows.get(from.getSelectedIndex()).getRank();
		int b = currentRows.get(to.getSelectedIndex()).getRank();
		int low = Math.min(a, b);
		int high = Math.max(a, b);
		List<Integer> ranks = new ArrayList<>();
		for (RankRow row : currentRows)
		{
			if (row.getRank() >= low && row.getRank() <= high)
			{
				ranks.add(row.getRank());
			}
		}

		openGameIconPicker("Icon for " + ranks.size() + (ranks.size() == 1 ? " rank" : " ranks"), true, choice ->
		{
			for (int rank : ranks)
			{
				plugin.setChoice(rank, choice);
			}
		});
	}

	private void addPlayerIcon()
	{
		String name = JOptionPane.showInputDialog(this, "Player name:", DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
		if (name == null || name.trim().isEmpty())
		{
			return;
		}

		openGameIconPicker("Icon for " + name.trim(), false, choice -> plugin.setPlayerIcon(name.trim(), choice));
	}

	private void copyShareCode()
	{
		String code = plugin.exportShareCode();
		if (code == null)
		{
			JOptionPane.showMessageDialog(this, "Log in and join your clan first.", DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null);
		JOptionPane.showMessageDialog(this, "Share code copied. Clanmates can press Paste code to get the same icons.",
			DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
	}

	private void pasteShareCode()
	{
		String code = JOptionPane.showInputDialog(this, "Paste a share code:", DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
		if (code == null || code.trim().isEmpty())
		{
			return;
		}

		try
		{
			int changed = plugin.importShareCode(code);
			if (changed < 0)
			{
				JOptionPane.showMessageDialog(this, "Log in and join your clan first.", DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Updated " + changed + (changed == 1 ? " rank." : " ranks."), DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(this, "That isn't a valid share code.", DIALOG_TITLE, JOptionPane.WARNING_MESSAGE);
		}
	}

	private static String escape(String text)
	{
		return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
