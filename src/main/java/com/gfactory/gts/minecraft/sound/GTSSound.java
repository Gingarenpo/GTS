package com.gfactory.gts.minecraft.sound;

import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;

/**
 * MinecraftのSoundクラスはデフォルトだとsounds/から読み込もうとしてしまう。
 * インメモリで扱う場合に不都合なので、サウンドを読み込む際にリソースパスを直接書き換えて
 * 直アクセスできるようにする。
 */
public class GTSSound extends Sound {


    /**
     * 指定したパラメーターでサウンドを作成する。
     * @param nameIn サウンドの名前。
     * @param volumeIn サウンドのボリューム。
     * @param pitchIn サウンドのピッチ。
     * @param weightIn サウンドの再生確率。
     * @param typeIn サウンドのタイプ。
     * @param p_i46526_6_ 難読化されていてよくわからない
     */
    public GTSSound(String nameIn, float volumeIn, float pitchIn, int weightIn, Type typeIn, boolean p_i46526_6_) {
        super(nameIn, volumeIn, pitchIn, weightIn, typeIn, p_i46526_6_);
    }

    /**
     * このサウンドのOGG実体を表す場所を返す。
     * ここでsoundsが付与されてしまっているので、素直にこのサウンドに登録された
     * リソースロケーションを返すことで無効化する。
     * @return
     */
    @Override
    public ResourceLocation getSoundAsOggLocation() {
        return new ResourceLocation(this.getSoundLocation().getResourceDomain(), this.getSoundLocation().getResourcePath());
    }
}
