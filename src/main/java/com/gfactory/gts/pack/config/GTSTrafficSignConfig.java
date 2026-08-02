package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

/**
 * 現在の仕様ではスクロールリストが事実上GTSConfigを継承したものしか使用できないため、
 * 看板として使用可能なテクスチャを自動で算出して動的に検索するためのダミーコンフィグとして保持。
 * IDはテクスチャのパスとする。モデルは使用せず、テクスチャのbaseのみを使用する。
 * サイズは可変
 */
public class GTSTrafficSignConfig extends GTSConfig<GTSConfig.GTSTexture> {
    @Override
    public void setDummy() {
        this.id = GTSPack.DUMMY; // generateIDで生成
        this.model = GTSPack.DUMMY; // 未使用
        this.textures = new GTSTexture();
        this.textures.base = GTSPack.DUMMY;
        this.size = 0.5f; // 未使用
    }

    /**
     * テクスチャを書き込み可能とする。テクスチャを設定すると、テクスチャに相当するIDが生成される。
     * @param texture
     */
    public void setTexture(String texture) {
        if (this.textures == null) this.textures = new GTSTexture();
        this.textures.base = texture;
        this.generateId();
    }

    /**
     * IDをテクスチャの名前から生成する
     */
    private void generateId() {
        // パス名を取得
        if (this.textures == null) return;
        if (this.textures.base == null) return;
        this.id = this.textures.base;
    }
}
