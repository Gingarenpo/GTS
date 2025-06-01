package com.gfactory.core.old;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * VBOとしてバッファで読み込むために必要な頂点データ、面データが格納されている
 * GPUの登録でIDが与えられるのでそれも格納する
 */
public class MQOVBOData {

    /**
     * GPU内で管理されるID。glBufferで使う
     */
    private int vid;

    /**
     * GPU内で管理されるID。
     */
    private int iid;

    /**
     * 頂点データ。XYZUVの順に格納されている。
     */
    private FloatBuffer v;

    /**
     * 面データ。三角面を基準として3頂点の頂点番号を格納。
     */
    private IntBuffer i;

    public MQOVBOData(IntBuffer i, int iid, int vid, FloatBuffer v) {
        this.i = i;
        this.iid = iid;
        this.vid = vid;
        this.v = v;
    }

    public IntBuffer getI() {
        return i;
    }

    public void setI(IntBuffer i) {
        this.i = i;
    }

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public FloatBuffer getV() {
        return v;
    }

    public void setV(FloatBuffer v) {
        this.v = v;
    }
}
