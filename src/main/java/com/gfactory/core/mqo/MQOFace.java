package com.gfactory.core.mqo;

/**
 * MQOの面情報を格納する。
 * あくまで、MQOに定義されている面として対象とするので、三角面以外に
 * 四角面、多角面である場合もある。GTSでは多角形の面は非推奨としているが、
 * 一応対応できるようにクラス的にはしておく。OpenGLの描画においては全て三角面で処理するので、
 * それに変換するメソッドも導入する。
 */
public final class MQOFace {

    /**
     * この面の頂点数
     */
    private int vLength;

    /**
     * この面を構成する頂点のID（オブジェクトごと）
     */
    private int[] vertexId;

    /**
     * この面のマテリアルID
     */
    private int materialId;

    /**
     * この面のUV
     */
    private double[][] uv;

    /**
     * 指定したパラメーターで初期化する。
     * 空のMQOFaceは認めない。また、同一パッケージからしか生成させない。
     * @param materialId 材質ID
     * @param uv UV座標をU,Vの順番で格納し、それを対応するvertexIdで並べた二次元配列
     * @param vertexId この面を構成するvertexId
     * @param vLength 頂点数
     */
    MQOFace(int materialId, double[][] uv, int[] vertexId, int vLength) {
        this.materialId = materialId;
        this.uv = uv;
        this.vertexId = vertexId;
        this.vLength = vLength;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public double[][] getUv() {
        return uv;
    }

    public void setUv(double[][] uv) {
        this.uv = uv;
    }

    public int[] getVertexId() {
        return vertexId;
    }

    public void setVertexId(int[] vertexId) {
        this.vertexId = vertexId;
    }

    public int getvLength() {
        return vLength;
    }

    public void setvLength(int vLength) {
        this.vLength = vLength;
    }

    /**
     * この面が三角形かを返す。
     * @return 三角形ならtrue
     */
    public boolean isTriangle() {
        return this.vLength == 3;
    }

    /**
     * この面が四角形かを返す。
     * @return 四角形ならtrue
     */
    public boolean isSquare() {
        return this.vLength == 4;
    }

    /**
     * この面を三角形の面にちりばめて返す。0番目の頂点を起点として、三角形に分割して
     * インスタンスをその数だけ作成し、返す。配列数は、頂点数-2となる。
     * 三角面だったとしてもその三角面が1個だけ変える
     * @return 0番目の頂点を軸として三角面に分割した各三角面の新規オブジェクト
     */
    public MQOFace[] splitTriangleFace() {
        MQOFace[] result = new MQOFace[vLength - 2];
        for (int i = 0; i < vLength - 2; i++) {
            result[i] = new MQOFace(materialId, new double[][] {uv[0], uv[i+2], uv[i+1]}, new int[] {vertexId[0], vertexId[i+2], vertexId[i+1]}, 3);
        }
        return result;
    }
}
