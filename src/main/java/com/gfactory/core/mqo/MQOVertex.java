package com.gfactory.core.mqo;

/**
 * MQOファイルの各オブジェクトの頂点情報をここに載せている。
 * 頂点情報自体は正直XYZの3つの頂点が記載されているだけだが、
 * 今後の拡張なども踏まえて独自クラスとして定義する。Vec3で事足りることには足りる。
 */
public final class MQOVertex {

    /**
     * この頂点のX座標。
     */
    private double x;

    /**
     * この頂点のY座標。
     */
    private double y;

    /**
     * この頂点のZ座標。
     */
    private double z;

    /**
     * 指定したXYZ軸で頂点を作成する。
     * @param x X軸
     * @param y Y軸
     * @param z Z軸
     */
    MQOVertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     * この頂点が等しいかどうかを判断する。
     * 座標が同一なら等しいとみなす。
     * @param o 比べるオブジェクト
     * @return 同じならTrue
     */
    public boolean equals(Object o) {
        if (!(o instanceof MQOVertex)) return false;
        MQOVertex v = (MQOVertex) o;
        return (v.x == this.x && v.y == this.y && v.z == this.z);

    }

    @Override
    public String toString() {
        return "MQOVertex{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
