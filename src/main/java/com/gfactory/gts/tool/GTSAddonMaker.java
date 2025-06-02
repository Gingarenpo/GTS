package com.gfactory.gts.tool;

import javax.swing.JFrame;

/**
 * GTSのアドオンを作成するためのツール。
 * Jarファイルをダブルクリックして直接実行するとこのソフトウェアが立ち上がる。
 * Modとして動くものではないので、Minecraftからこれを起動しようと思っても無視される。
 * ただし、一応同一のJVMで動かすことはできる（mainメソッドを呼べばいいだけ）。
 * 
 * Swingを使用してUIを構築しており、保守性には欠けるが……
 */
public class GTSAddonMaker extends JFrame {

	public static void main(String[] args) {
		GTSAddonMaker m = new GTSAddonMaker();
		m.setTitle("GTS");
		m.setSize(1280, 720);
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m.setLocationRelativeTo(null);
		m.setVisible(true);

	}

}
