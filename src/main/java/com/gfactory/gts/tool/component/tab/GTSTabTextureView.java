package com.gfactory.gts.tool.component.tab;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.gfactory.gts.tool.GTSPackMaker;
import com.gfactory.gts.tool.helper.I18n;

/**
 * テクスチャの閲覧ができるイメージビューを定義したもの。
 * 単純な画像ビューア扱いとなっており、これ単体で編集は今のところできない。
 */
public class GTSTabTextureView extends JPanel {
	
	public GTSTabTextureView(File file) {
		this.setLayout(new BorderLayout());
		// 画像の読み込みを試みる
		try {
			BufferedImage image = ImageIO.read(file);
			TexturePanel panel = new TexturePanel(image);
			this.add(panel, BorderLayout.CENTER);
		} catch (IOException e) {
			// 読み込みに失敗した場合はエラーダイアログを出す
			JOptionPane.showMessageDialog(GTSPackMaker.window, I18n.format("message.failed.texture"), I18n.format("message.title.failed"), JOptionPane.ERROR_MESSAGE);
		}
		
	}

	/**
	 * 画像表示用ビューワパネル
	 */
	public static class TexturePanel extends JPanel {
		/**
		 * 表示すべき画像
		 */
		private BufferedImage image;
		
		public TexturePanel(BufferedImage image) {
			this.image = image;
		}

		/**
		 * 画像を表示サイズに合わせてリサイズする
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			float zoom = Math.min(this.getWidth() / image.getWidth(), this.getHeight() / image.getHeight());
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
		}
		
		
	}
}
