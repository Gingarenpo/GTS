package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

import java.util.ArrayList;

/**
 * 交通信号機のパックを追加するためのコンフィグファイル。
 * 交通信号機を動かすために必要なフィールドを追加している。
 */
public class GTSTrafficLightConfig<T> extends GTSConfig<GTSTrafficLightConfig.GTSTrafficLightTexture> {


    private final ArrayList<GTSTrafficLightPattern> patterns = new ArrayList<>();

    /**
     * 光る光らない関係ないレンダリングするオブジェクトをここで指定する
     */
    private final ArrayList<String> body = new ArrayList<>();

    /**
     * 光る光らないが関係するレンダリングするオブジェクトをここで指定する
     */
    private final ArrayList<String> light = new ArrayList<>();

    public GTSTrafficLightConfig() {}

    public void setDummy() {
        this.id = GTSPack.DUMMY_TRAFFIC_LIGHT;
        this.model = GTSPack.DUMMY_TRAFFIC_LIGHT;
        this.size = 2.5;
        this.opacity = 0;
        this.textures = new GTSTrafficLightTexture(GTSPack.DUMMY_TRAFFIC_LIGHT, GTSPack.DUMMY_TRAFFIC_LIGHT, GTSPack.DUMMY_TRAFFIC_LIGHT);
        this.light.add("g300");
        this.light.add("y300");
        this.light.add("r300");
        this.body.add("body");
        this.body.add("g");
        this.body.add("y");
        this.body.add("r");
        this.body.add("normalg");
        this.body.add("normaly");
        this.body.add("normalr");
    }

    public ArrayList<GTSTrafficLightPattern> getPatterns() {
        return patterns;
    }

    public ArrayList<String> getBody() {
        return body;
    }

    public ArrayList<String> getLight() {
        return light;
    }

    /**
     * 交通信号機はテクスチャが3つあるためそちらを利用する
     */
    public static class GTSTrafficLightTexture extends GTSConfig.GTSTexture {
        private String light;
        private String noLight;

        public GTSTrafficLightTexture() {}

        /**
         * テクスチャを全て指定して作成。ダミーファイル作成用。
         * @param base ベース
         * @param light ライト
         * @param noLight ノーライト
         */
        private GTSTrafficLightTexture(String base, String light, String noLight) {
            this.base = base;
            this.light = light;
            this.noLight = noLight;
        }

        public String getLight() {
            return light;
        }

        public String getNoLight() {
            return noLight;
        }
    }

    /**
     * 交通信号機の点灯パターン
     * どのオブジェクトがどのパターンでどのように光るのかを記録するための部分
     */
    public static class GTSTrafficLightPattern {
        /**
         * このパターンの名前。同一のコンフィグ内で名前の重複は禁止。
         */
        private String name;

        /**
         * このパターンで点灯するオブジェクトの名前。もっぱらMQO
         */
        private ArrayList<String> objects = new ArrayList<>();

        /**
         * このパターンの点滅周期をTickで表す。1秒 = 20Tick。
         * 周期なので、20Tickにすると点灯→10Tickで消灯→10Tickで点灯のようにふるまう。
         * 0にすると常時点灯
         */
        private int tick;

        public GTSTrafficLightPattern() {}

        public String getName() {
            return name;
        }

        public ArrayList<String> getObjects() {
            return objects;
        }

        public int getTick() {
            return tick;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTick(int tick) {
            this.tick = tick;
        }

        public void setObjects(ArrayList<String> objects) {
            this.objects = objects;
        }

        @Override
        public String toString() {
            return "GTSTrafficLightPattern{" +
                    "name='" + name + '\'' +
                    ", objects=" + objects +
                    ", tick=" + tick +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GTSTrafficLightConfig{" +
                "textures=" + textures +
                ", patterns=" + patterns +
                "} " + super.toString();
    }
}
