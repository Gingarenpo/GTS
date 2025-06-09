package com.gfactory.gts.tool.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.gfactory.gts.tool.event.ProjectEvent;
import com.gfactory.gts.tool.helper.I18n;

/**
 * GTS Pack MakerのGUIに表示するメニューバー
 */
public class GTSMenuBar extends JMenuBar {
	
	private JFrame window;
	
	public JMenu menuFile = new JMenu(I18n.format("menu.file"));
	public JMenuItem menuCreateProject = new JMenuItem(I18n.format("menu.file.createProject"));
	public JMenuItem menuImportPack = new JMenuItem(I18n.format("menu.file.importPack"));
	public JMenuItem menuExportPack = new JMenuItem(I18n.format("menu.file.exportPack"));
	
	public GTSMenuBar(JFrame window) {
		super();
		this.window = window;
		init();
	}
	
	private void init() {
		
		// ファイルを追加
		this.menuFile.setMnemonic(KeyEvent.VK_F);
		this.add(menuFile);
		
		// ファイルメニューのサブメニューを追加
		// 新規プロジェクト作成
		this.menuCreateProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		this.menuCreateProject.setToolTipText(I18n.format("menu.tooltip.createProject"));
		this.menuCreateProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProjectEvent.createProject();
			}
		});
		menuFile.add(menuCreateProject);
		this.menuImportPack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
		this.menuImportPack.setToolTipText(I18n.format("menu.tooltip.importPack"));
		menuFile.add(menuImportPack);
		this.menuExportPack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
		this.menuExportPack.setToolTipText(I18n.format("menu.tooltip.exportPack"));
		menuFile.add(menuExportPack);
		
		// 有効か無効かを切り替え
		this.menuExportPack.setEnabled(false);
	}

}
