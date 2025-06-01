package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

/**
 * 交通信号制御機のコンフィグ
 * 特に追加すべき項目は今のところないが、系統制御の実装などでもしかしたら使うかもしれないので
 */
public class GTSTrafficControllerConfig extends GTSConfig<GTSConfig.GTSTexture>{
    @Override
    public void setDummy() {
        this.id = GTSPack.DUMMY_TRAFFIC_CONTROLLER;
        this.model = GTSPack.DUMMY_TRAFFIC_CONTROLLER;
        this.textures = new GTSTexture();
        this.textures.base = GTSPack.DUMMY_TRAFFIC_CONTROLLER;
        this.size = 2.0;
        this.opacity = 0.0;
        this.originalPosition = new double[] {0, 0.5, 0};
    }
}
