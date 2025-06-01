package com.gfactory.gts.pack.config;

/**
 * GTSのPack内の情報1つを表す最小単位。
 * JSONファイルで構成され、中身に必要な情報を入力することでアドオンで使用可能なデータと認識する。
 * 詳細な仕様は別記。
 *
 * このクラスはAbstractで、完全にベースとなっている。従ってこのインスタンスを作るのは不可能。
 */
public abstract class GTSConfig<T extends GTSConfig.GTSTexture> {

    /**
     * このコンフィグのID。パック単位で重複してはならない。半角英数字_-のみ受け付けるようにする（将来的な誤爆を防ぐため）
     */
    protected String id;

    /**
     * このコンフィグが使用するモデル。どのパターンであってもモデルが存在するはず。ZIPファイル内の相対パスで記載する。
     */
    protected String model;

    /**
     * このコンフィグが使用するテクスチャを格納する。
     * テクスチャは基本的にGTSTextureになる
     */
    protected T textures;

    /**
     * このモデルを配置したときのサイズ係数を指定する。モデルは全て1ブロックの大きさに最適化されるため、
     * 最も長い辺のブロック数を入力することで調整できる。省略することもでき、その場合は1.0となる。
     */
    protected double size = 1.0;

    /**
     * このモデルの透過度を示す。と言っても、現状使用しない。0で完全不透明、1で完全透明。
     */
    protected double opacity = 0.0;

    /**
     * このモデルのデフォルト描画位置を示す。これにより、TileEntityでこのモデルが選ばれたときに
     * そのコンフィグのポジションを利用して再設定を行う。存在しないパラメータでもあるので、
     * ない場合は原点の位置がそのまま使われる。XYZの順で格納される。
     */
    protected double[] originalPosition = new double[3];

    /**
     * JSONからインスタンスを使用するために必要なもの
     */
    public GTSConfig() {}

    /**
     * 暫定：このコンフィグの文字列表現
     * @return 今後変更するかも
     */
    @Override
    public String toString() {
        return "["+this.id+"] model="+this.model+", size="+this.size+", opacity="+this.opacity+",";
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public T getTextures() {
        return textures;
    }

    public double getSize() {
        return size;
    }

    public double getOpacity() {
        return opacity;
    }

    /**
     * 普通使わないが、ダミーコンフィグを作成することができるもの。
     * 各継承先において必要なものが違うため、明示的に実装させる。
     * 必要に応じて、親元のフィールドも使用すること。
     */
    public abstract void setDummy();

    /**
     * テクスチャの基礎。どのモデルも基本的に「base」だけは持っており、
     * あと光るか光らないかで結構変わるのでここを継承させることで
     * GSONの重複エラーを失くす
     */
    public static class GTSTexture {
        /**
         * 基本となるテクスチャ
         */
        protected String base;

        public GTSTexture() {}

        @Override
        public String toString() {
            return "GTSTexture{" +
                    "base='" + base + '\'' +
                    '}';
        }

        public String getBase() {
            return base;
        }
    }

    public double[] getOriginalPosition() {
        return originalPosition;
    }
}
