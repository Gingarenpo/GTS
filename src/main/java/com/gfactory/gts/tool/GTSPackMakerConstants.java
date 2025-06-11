package com.gfactory.gts.tool;

import javax.swing.ImageIcon;

/**
 * 定数的なものを格納しておくクラス。
 * このクラス自体はインスタンスの作成を行うものではなく、どこからでも呼び出せる定数を全て保持しておくもの。
 * そのため、全てpublic static finalである
 */
public final class GTSPackMakerConstants {
	
	/**
	 * コンストラクタは呼び出し不能
	 */
	private GTSPackMakerConstants() {}
	
	/**
	 * 信号機アイコン
	 */
	public static final ImageIcon ICON_LIGHT = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/light.png"));
	
	/**
	 * 制御機アイコン
	 */
	public static final ImageIcon ICON_CONTROLLER = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/controller.png"));
	
	/**
	 * ポールアイコン
	 */
	public static final ImageIcon ICON_POLE = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/pole.png"));
	
	/**
	 * 閉じるアイコン
	 */
	public static final ImageIcon ICON_CLOSE = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/close.png"));
	
	/**
	 * 閉じるアイコン（ホバー時）
	 */
	public static final ImageIcon ICON_CLOSE_OVER = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/close_over.png"));
	
	/**
	 * モデルアイコン
	 */
	public static final ImageIcon ICON_MODEL = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/mqo.png"));
	
	/**
	 * テクスチャアイコン
	 */
	public static final ImageIcon ICON_TEXTURE = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/texture.png"));
	
	/**
	 * ファイル作成用アイコン
	 */
	public static final ImageIcon ICON_NEW = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/new.png"));
	
	/**
	 * ディレクトリ用アイコン
	 */
	public static final ImageIcon ICON_DIRECTORY = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/directory.png"));
	
	/**
	 * インポート用アイコン
	 */
	public static final ImageIcon ICON_IMPORT = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/import.png"));
	
	/**
	 * エクスポート用アイコン
	 */
	public static final ImageIcon ICON_EXPORT = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/export.png"));
	
	/**
	 * ただのファイルアイコン
	 */
	public static final ImageIcon ICON_FILE = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/file.png"));
	
	/**
	 * サウンドアイコン
	 */
	public static final ImageIcon ICON_SOUND = new ImageIcon(GTSPackMakerConstants.class.getResource("/tools/icons/sound.png"));
	
	
	

}
