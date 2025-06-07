package com.gfactory.gts.minecraft.sound;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;

/**
 * 直接サウンドを再生するため、サウンドを登録するラッパー。
 * 実装しなくてはならないものが多すぎるが、基本的にここでクライアントで
 * 流す用のサウンドイベントの代わりのようなものになる。
 */
public abstract class GTSSoundEvent<T extends GTSTileEntity> implements ISound {

    /**
     * このサウンドが鳴る座標X
     */
    protected float x;

    /**
     * このサウンドが鳴る座標Y
     */
    protected float y;

    /**
     * このサウンドが鳴る座標Z
     */
    protected float z;

    /**
     * サウンドアクセッサ？
     */
    protected SoundEventAccessor accessor;

    /**
     * TileEntityそのもの
     */
    protected T te;


    public GTSSoundEvent(T te) {
        // 座標の登録
        this.x = te.getPos().getX();
        this.y = te.getPos().getY();
        this.z = te.getPos().getZ();

        // リソースロケーションは各クラスに委ねる
        this.te = te;
    }


    /**
     * このサウンドイベントのサウンドを指し示すリソースロケーションを返す。
     * @return
     */
    @Override
    public abstract ResourceLocation getSoundLocation();

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        this.accessor = new SoundEventAccessor(this.getSoundLocation(), null);
        return this.accessor;
    }

    /**
     * 実際のサウンドインスタンスを返す。
     * ここでは、GTSSoundとして定義したサウンドを返す。
     * @return
     */
    @Override
    public Sound getSound() {
        return new GTSSound(this.getSoundLocation().toString(), getVolume(), getPitch(), 1, Sound.Type.FILE, false);
    }

    /**
     * このサウンドイベントのカテゴリを返す。
     * @return
     */
    @Override
    public SoundCategory getCategory() {
        return SoundCategory.BLOCKS;
    }

    /**
     * このサウンドがリピート可能かどうかを返す。
     * @return
     */
    @Override
    public boolean canRepeat() {
        return false;
    }

    /**
     * このサウンドがリピートする際の遅延時間を指定する。
     * リピートしないので0
     * @return
     */
    @Override
    public int getRepeatDelay() {
        return 0;
    }

    /**
     * デフォルトのボリュームを返す。
     * @return
     */
    @Override
    public float getVolume() {
        return 1;
    }

    /**
     * デフォルトのピッチを返す。
     * @return
     */
    @Override
    public float getPitch() {
        return 1;
    }

    /**
     * このサウンドが鳴るべき場所を返す。
     * @return
     */
    @Override
    public float getXPosF() {
        return this.x;
    }

    /**
     * このサウンドが鳴るべき場所を返す。
     * @return
     */
    @Override
    public float getYPosF() {
        return this.y;
    }

    /**
     * このサウンドが鳴るべき場所を返す。
     * @return
     */
    @Override
    public float getZPosF() {
        return this.z;
    }

    /**
     * よくわからんが減衰アルゴリズム？
     * LINEARしか定数がないのでそれを返す
     * @return
     */
    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.LINEAR;
    }
}
