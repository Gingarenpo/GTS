package com.gfactory.gts.tool;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.gfactory.gts.tool.component.GTSMenuBar;
import com.gfactory.gts.tool.helper.I18n;
import com.gfactory.gts.tool.project.GTSPackProject;

/**
 * GTSのアドオンを作成するためのツール。
 * Jarファイルをダブルクリックして直接実行するとこのソフトウェアが立ち上がる。
 * Modとして動くものではないので、Minecraftからこれを起動しようと思っても無視される。
 * ただし、一応同一のJVMで動かすことはできる（mainメソッドを呼べばいいだけ）。
 * 
 * Swingを使用してUIを構築しており、保守性には欠けるが……
 * なおこのクラスはエントリークラスであり、画面とかは別で管理している
 */
public class GTSPackMaker {
	
	/**
	 * メインウィンドウ
	 */
	public static JFrame window = new JFrame();
	
	/**
	 * バージョン
	 */
	public static final String VERSION = "0.1b - GTS2";
	
	/**
	 * ステータスバー
	 */
	public static JLabel statusBar = new JLabel(I18n.format("status.noProjectOpen"));

	/**
	 * 現在開いているプロジェクトのディレクトリ位置
	 */
	public static GTSPackProject project;
	
	public static void main(String[] args) {
		init();
	}
	
	public static void init() {
		// Look and Feelをシステムに応じたものに変更
		try {
			// Windowsライクな見た目にトライ
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			try {
				// Linuxライクな見た目にトライ
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} catch (Exception e2) {
				e2.printStackTrace();
				// 仕方ないのでMetalで
			}
		}
		
		// ウィンドウ初期設定
		window.setTitle(I18n.format("core.title", VERSION));
		window.setSize(1280, 720);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		
		// メニューバー設定
		window.setJMenuBar(new GTSMenuBar(window));
		window.setVisible(true);
		
		// ステータスバー設定
		JPanel statusPanel = new JPanel();
		statusPanel.setBackground(new Color(224, 224, 224));
		statusPanel.setBorder(new EtchedBorder());
		statusPanel.add(statusBar);
		window.getContentPane().add(statusPanel, BorderLayout.SOUTH);
	}

}
