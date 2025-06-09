package com.gfactory.gts.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.gfactory.gts.tool.component.GTSMainTabPane;
import com.gfactory.gts.tool.component.GTSMenuBar;
import com.gfactory.gts.tool.component.GTSSideTreeView;
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
	public static final String VERSION = "0.1b - GTS2.0";
	
	/**
	 * ステータスバー
	 */
	public static JLabel statusBar;
	
	/**
	 * サイドバー
	 */
	public static GTSSideTreeView sideView;
	
	/**
	 * メインエディタのタブパネル
	 */
	public static GTSMainTabPane mainView;
	
	/**
	 * 各種設定項目を保存しておくところ
	 */
	public static GTSPackMakerConfig config = new GTSPackMakerConfig();

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
			SwingUtilities.updateComponentTreeUI(window);
		} catch (Exception e) {
			try {
				// Linuxライクな見た目にトライ
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				SwingUtilities.updateComponentTreeUI(window);
			} catch (Exception e2) {
				e2.printStackTrace();
				// 仕方ないのでMetalで
			}
		}
		
		// ウィンドウ初期設定
		window.setTitle(I18n.format("core.title", VERSION));
		window.setSize(config.getWindowSize());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		
		
		// メニューバー設定
		window.setJMenuBar(new GTSMenuBar(window));
		window.setVisible(true);
		
		// ステータスバー設定
		statusBar = new JLabel(I18n.format("status.noProjectOpen"));
		statusBar.setHorizontalTextPosition(JLabel.LEFT);
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new EtchedBorder());
		statusPanel.add(statusBar);
		window.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		// サイドバー設定
		sideView = new GTSSideTreeView();
		window.getContentPane().add(sideView, BorderLayout.WEST);
		
		// タブバー設定
		mainView = new GTSMainTabPane();
		window.getContentPane().add(mainView, BorderLayout.CENTER);
	
	}
	
	/**
	 * PackMakerの設定ファイルをJSON形式で読み書きするためのもの
	 * デフォルトのパスはGTSディレクトリがあればそこの中、無ければ直下
	 */
	public static class GTSPackMakerConfig {
		
		private int[] size = new int[] {1280, 720};
		
		public GTSPackMakerConfig() {
			// ダミーコンフィグを設定
		}
		
	
		/**
		 * ウィンドウサイズを返す
		 * @return
		 */
		public Dimension getWindowSize() {
			return new Dimension(size[0], size[1]);
		}
	}

}
