package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

import java.util.ArrayList;

/**
 * アームのコンフィグ。
 * TileEntityを必要としない。アームの使用オブジェクトとして付け根（ポール・信号機）と
 * 繰り返し部分があり、それで区別する。
 */
public class GTSTrafficArmConfig extends GTSConfig<GTSConfig.GTSTexture> {

    /**
     * 付け根のオブジェクト。
     */
    private ArrayList<String> edgeObjects = new ArrayList<>();

    /**
     * 繰り返す（引き延ばす）オブジェクト。引き延ばされるので平行を推奨。これが必須。
     *
     */
    private ArrayList<String> baseObjects = new ArrayList<>();

    /**
     * 信号機とかの付け根のオブジェクト。
     */
    private ArrayList<String> endObjects = new ArrayList<>();

    /**
     * ノーマルのオブジェクトを設置できないレベルの長さである場合、それでもedgeObjectsの描画を絶対に行うか
     */
    private boolean drawStartPrimary = true;

    @Override
    public void setDummy() {
        this.baseObjects.add("normal");
        this.edgeObjects.add("top");
        this.endObjects.add("normal");
        this.id = GTSPack.DUMMY_TRAFFIC_ARM;
        this.model = GTSPack.DUMMY_TRAFFIC_ARM;
        this.textures = new GTSTexture();
        this.textures.base = GTSPack.DUMMY_TRAFFIC_ARM;
        this.size = 1.0;
        this.opacity = 1.0;
    }

    public ArrayList<String> getBaseObjects() {
        return baseObjects;
    }

    public void setBaseObjects(ArrayList<String> baseObjects) {
        this.baseObjects = baseObjects;
    }

    public ArrayList<String> getEdgeObjects() {
        return edgeObjects;
    }

    public void setEdgeObjects(ArrayList<String> edgeObjects) {
        this.edgeObjects = edgeObjects;
    }

    public ArrayList<String> getEndObjects() {
        return endObjects;
    }

    public void setEndObjects(ArrayList<String> endObjects) {
        this.endObjects = endObjects;
    }

    public boolean isDrawStartPrimary() {
        return drawStartPrimary;
    }

    public void setDrawStartPrimary(boolean drawStartPrimary) {
        this.drawStartPrimary = drawStartPrimary;
    }
}
