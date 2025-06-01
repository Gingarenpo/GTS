package com.gfactory.core.mqo;

import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * VAOの代わりとして、VBOオブジェクトに必要な情報を保持するクラス。
 * 頂点情報、面情報、UV情報、法線情報を維持する。
 */
public class MQOVBOData {

    /**
     * 頂点・UV・法線をXYZUVNNNの8要素ごとに格納したバッファ
     * あるいはXYZUVCCCでカラーを指定していたりする（法線が言うこと聞かないとき）
     */
    private final FloatBuffer vertexVBO;

    /**
     * 面情報を三角形に分割して読み込んだ時の頂点番号を3つずつ格納したバッファ
     */
    private final IntBuffer faceIndex;

    /**
     * 頂点情報を格納したバッファがGPUに読み込まれたさいのバッファID
     */
    private int vid = -1;

    /**
     * 法線を使うかどうか。678の要素の意味が変わる
     */
    private boolean useNormal = false;


    /**
     * 各パラメータを指定して、VBODataをひとまとめにする。
     * バッファの数は切り詰められることを想定している。
     * @param faceIndex 面情報バッファ
     * @param vertexVBO 頂点情報バッファ
     */
    MQOVBOData(IntBuffer faceIndex, FloatBuffer vertexVBO) {
        this.faceIndex = faceIndex;
        this.vertexVBO = vertexVBO;
    }

    public IntBuffer getFaceIndex() {
        return faceIndex;
    }


    public FloatBuffer getVertexVBO() {
        return vertexVBO;
    }

    public int getVid() {
        return vid;
    }

    /**
     * GPUにロードされているかを返す。
     * @return GPUロード済みであればtrue
     */
    public boolean isGPULoaded() {
        return (this.vid != -1);
    }

    /**
     * このVBOデータをバッファに登録する。
     * OpenGLコンテキストが既に用意されている必要がある。
     * このメソッド終了後、isGPULoadedを呼び出しておくと安心。
     */
    public void loadGPU() {
        // 頂点情報をバインド
        this.vid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vid);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertexVBO, GL15.GL_STATIC_DRAW);

    }

    public boolean isUseNormal() {
        return useNormal;
    }

    public void setUseNormal(boolean useNormal) {
        this.useNormal = useNormal;
    }
}
