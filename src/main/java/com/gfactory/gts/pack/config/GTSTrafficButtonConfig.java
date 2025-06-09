package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

/**
 * 押ボタン箱のコンフィグ。
 * 現時点では、音声ファイルを選択することが可能。これが特有の目印となる。
 */
public class GTSTrafficButtonConfig<T> extends GTSConfig<GTSTrafficButtonConfig.GTSTrafficButtonTexture>{

    /**
     * 押ボタンの音声群
     */
    private GTSTrafficButtonAudio audios;

    /**
     * 常時鳴る音をどの頻度で鳴らすか。Tickで指定する。
     * 0を指定すると、鳴らさない。負の数を指定することはできない。
     */
    private int baseSoundTick = 0;

    @Override
    public void setDummy() {
        this.id = GTSPack.DUMMY_TRAFFIC_BUTTON;
        this.model = GTSPack.DUMMY_TRAFFIC_BUTTON;
        this.audios = new GTSTrafficButtonAudio();
        this.audios.base = GTSPack.DUMMY_TRAFFIC_BUTTON + ".ogg";
        this.audios.detected = GTSPack.DUMMY_TRAFFIC_BUTTON + ".ogg";
        this.textures = new GTSTrafficButtonTexture();
        this.textures.base = GTSPack.DUMMY_TRAFFIC_BUTTON;
        this.textures.detected = GTSPack.DUMMY_TRAFFIC_BUTTON;
        this.baseSoundTick = 0;
        this.size = 0.4;
        this.opacity = 1.0;
    }

    public GTSTrafficButtonAudio getAudios() {
        return audios;
    }

    public void setAudios(GTSTrafficButtonAudio audios) {
        this.audios = audios;
    }

    public int getBaseSoundTick() {
        return baseSoundTick;
    }

    public void setBaseSoundTick(int baseSoundTick) {
        if (baseSoundTick < 0) baseSoundTick = 0; // 負の数は強制的に0にする
        this.baseSoundTick = baseSoundTick;
    }

    /**
     * 通常状態のbaseに加え、detectedで検知後のテクスチャを保存する
     */
    public static class GTSTrafficButtonTexture extends GTSConfig.GTSTexture {

        private String detected;

        public String getDetected() {
            return detected;
        }

        public void setDetected(String detected) {
            this.detected = detected;
        }
    }

    /**
     * この押ボタン箱の音声ファイル。
     * 常時鳴るものと、押されたときになるものの2種類を登録する。
     * 常時鳴るものは鳴らさないことも可能。音響の「ポポッ」みたいな音を想定している。
     * 押ボタン箱から音声案内を流すのは非対応。
     */
    public static class GTSTrafficButtonAudio {

        /**
         * 検知したときに1回だけ鳴らす音
         */
        private String detected;

        /**
         * 常時鳴るもの。別の設定で鳴らさないことをできるので鳴らしたくない場合は検知音を入れておくといい。
         */
        private String base;

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getDetected() {
            return detected;
        }

        public void setDetected(String detected) {
            this.detected = detected;
        }
    }

}
