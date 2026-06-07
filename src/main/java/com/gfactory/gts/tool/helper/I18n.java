package com.gfactory.gts.tool.helper;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * MinecraftのI18nを模倣したクラス
 * インスタンスが作成されるとリソースバンドルが読み込まれるだけ
 */
public final class I18n {
	
	private static ResourceBundle bundle = UTF8ResourceBundle.getBundle("tools/packmaker", Locale.getDefault());
	
	/**
	 * 指定したキーと指定したフォーマットを用いて、リソースバンドルからテンプレートを取得して返す。
	 * ただし、存在しない場合はキーをそのまま返す。
	 * @param key
	 * @param args
	 * @return
	 */
	public static String format(String key, String... args) {
		try {
			String template = bundle.getString(key);
			return MessageFormat.format(template, (Object) args);
		} catch (MissingResourceException e) {
			return key;
		}
	}

}
