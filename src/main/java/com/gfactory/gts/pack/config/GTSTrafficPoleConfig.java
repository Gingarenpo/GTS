package com.gfactory.gts.pack.config;

import com.gfactory.gts.pack.GTSPack;

import java.util.ArrayList;

/**
 * ポールのコンフィグファイル
 * ポールは上下の接続が可能。
 * 勿論、どうでもいい場合は全部同じオブジェクトを指定することも可能。配列で指定する。
 *
 */
public class GTSTrafficPoleConfig extends GTSConfig<GTSConfig.GTSTexture> {

    /**
     * 通常オブジェクト。絶対に指定する必要がある。
     */
    private ArrayList<String> normalObject;

    /**
     * アップジョイントオブジェクト。上方向に接続されている場合
     */
    private ArrayList<String> upJointObject;

    /**
     * 下方向に接続されている場合
     */
    private ArrayList<String> bottomJointObject;

    /**
     * 上下どっちも接続されている場合
     */
    private ArrayList<String> fullJointObject;

    @Override
    public void setDummy() {
        ArrayList<String> normalObject = new ArrayList<>();
        normalObject.add("base");
        this.normalObject = normalObject;
        ArrayList<String> upJointObject = new ArrayList<>();
        upJointObject.add("bottom");
        this.upJointObject = upJointObject;
        ArrayList<String> bottomJointObject = new ArrayList<>();
        bottomJointObject.add("top");
        this.bottomJointObject = bottomJointObject;
        ArrayList<String> fullJointObject = new ArrayList<>();
        fullJointObject.add("base");
        this.fullJointObject = fullJointObject;
        this.size = 1.0;
        this.opacity = 1.0;
        this.textures = new GTSTexture();
        this.textures.base = GTSPack.DUMMY_TRAFFIC_POLE;
        this.id = GTSPack.DUMMY_TRAFFIC_POLE;
        this.model = GTSPack.DUMMY_TRAFFIC_POLE;
    }

    public ArrayList<String> getBottomJointObject() {
        return bottomJointObject;
    }

    public void setBottomJointObject(ArrayList<String> bottomJointObject) {
        this.bottomJointObject = bottomJointObject;
    }

    public ArrayList<String> getFullJointObject() {
        return fullJointObject;
    }

    public void setFullJointObject(ArrayList<String> fullJointObject) {
        this.fullJointObject = fullJointObject;
    }

    public ArrayList<String> getNormalObject() {
        return normalObject;
    }

    public void setNormalObject(ArrayList<String> normalObject) {
        this.normalObject = normalObject;
    }

    public ArrayList<String> getUpJointObject() {
        return upJointObject;
    }

    public void setUpJointObject(ArrayList<String> upJointObject) {
        this.upJointObject = upJointObject;
    }
}
