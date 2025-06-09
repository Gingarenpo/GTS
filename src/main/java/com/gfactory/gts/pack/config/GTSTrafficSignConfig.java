package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

/**
 * コンフィグは原則ダミーで、モデルパックでどうにかできる次元ではない。
 * そのため適当なダミーモデルを用意しておく。
 */
public class GTSTrafficSignConfig extends GTSConfig<GTSConfig.GTSTexture> {
    @Override
    public void setDummy() {
        this.id = GTSPack.DUMMY;
        this.model = GTSPack.DUMMY;
        this.textures = new GTSTexture();
        this.textures.base = GTSPack.DUMMY;
        this.size = 0.5f;
    }
}
