package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.helper.GNBTHelper;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficPoleConfig;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

/**
 * 信号柱（ポール）のTileEntity。
 * アームに関する情報もここに記載される。アームは同一ブロックから色んな方向に延ばすことが可能なため、
 * アームのコンフィグと座標を保持することで描画に使う。
 */
public class GTSTileEntityTrafficPole extends GTSTileEntity<GTSTrafficPoleConfig>{

    /**
     * 接続されているアームの情報を格納する。
     * アームはTileEntityとして独立はさせないことにする。
     */
    private ArrayList<TrafficArm> jointArms = new ArrayList();

    /**
     * 上方向に自分自身がいるか？
     */
    private boolean upJoint = false;

    /**
     * 下方向に自分自身がいるか？
     */
    private boolean bottomJoint = false;

    public GTSTileEntityTrafficPole() {
        this.setDummy();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setDummy() {
        // ダミーファイルをセットする
        GTSPack p = GTS.LOADER.getDummy();
        if (p == null) {
            // ダミーパックが使えない場合、致命的なエラーとなる
            // TODO: とりあえず落としているけど本当はなんかリカバリーすべき
            throw new RuntimeException("Dummy Model cannot be loaded.");
        }
        this.pack = p;
        GTSTrafficPoleConfig config = new GTSTrafficPoleConfig();
        config.setDummy();
        this.setConfig(config);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        this.upJoint = GNBTHelper.getBooleanWithValue(compound, "gts_upjoint", false);
        this.bottomJoint = GNBTHelper.getBooleanWithValue(compound, "gts_bottomjoint", false);
    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("gts_upjoint", this.upJoint);
        compound.setBoolean("gts_bottomjoint", this.bottomJoint);
        return compound;
    }

    public ArrayList<TrafficArm> getJointArms() {
        return jointArms;
    }

    public void setJointArms(ArrayList<TrafficArm> jointArms) {
        this.jointArms = jointArms;
    }

    public boolean isBottomJoint() {
        return bottomJoint;
    }

    public void setBottomJoint(boolean bottomJoint) {
        this.bottomJoint = bottomJoint;
    }

    public boolean isUpJoint() {
        return upJoint;
    }

    public void setUpJoint(boolean upJoint) {
        this.upJoint = upJoint;
    }

    /**
     * 信号アームの情報をひとまとめにしたもの
     */
    public static class TrafficArm {

    }
}
