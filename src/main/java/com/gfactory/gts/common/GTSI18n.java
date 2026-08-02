package com.gfactory.gts.common;

import net.minecraft.util.text.TextComponentTranslation;

/**
 * I18nのログ出力をサーバー側でも対応できるようにしたもの
 */
public class GTSI18n {

    /**
     * ログの出力にサーバーパケット走らせるためのもの
     * @param key
     * @param args
     * @return
     */
    public static String i18n(String key, Object... args) {
        return new TextComponentTranslation(key, args).getFormattedText();
    }
}
