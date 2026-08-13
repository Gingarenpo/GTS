package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

import java.util.HashMap;

/**
 * スピーカーのコンフィグ。
 * 鳴らす音とその所在を格納した専用のHashMapを保持する。
 */
public class GTSTrafficSpeakerConfig extends GTSConfig<GTSConfig.GTSTexture> {

    /**
     * 値の方にはそのキーが選択されたときに鳴らす音を入れる
     */
    private final HashMap<String, String> sounds = new HashMap<>();

    @Override
    public void setDummy() {
        this.id = GTSPack.DUMMY_TRAFFIC_SPEAKER;
        this.size = 1;
        GTSTexture texture = new GTSTexture();
        texture.base = GTSPack.DUMMY_TRAFFIC_CONTROLLER;
        this.textures = texture;
        this.model = GTSPack.DUMMY_TRAFFIC_SPEAKER;
        this.sounds.put("green", GTSPack.DUMMY_TRAFFIC_SPEAKER + "_dummy.ogg");
        this.sounds.put("flush", GTSPack.DUMMY_TRAFFIC_SPEAKER + "_flush.ogg");
    }

    /**
     * このスピーカーが鳴らすことのできる音楽のキーとそのサウンドの諸元を返す
     * @return サウンド
     */
    public HashMap<String, String> getSounds() {
        return this.sounds;
    }
}
