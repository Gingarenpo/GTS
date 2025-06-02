package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.helper.GNBTHelper;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficPoleConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;

/**
 * 信号柱（ポール）のTileEntity。
 * アームに関する情報もここに記載される。アームは同一ブロックから色んな方向に延ばすことが可能なため、
 * アームのコンフィグと座標を保持することで描画に使う。
 */
public class GTSTileEntityTrafficPole extends GTSTileEntity<GTSTrafficPoleConfig> implements IGTSAttachable<GTSTileEntityTrafficArm> {

    /**
     * 接続されているアームの情報を格納する。
     */
    private ArrayList<GTSTileEntityTrafficArm> jointArms = new ArrayList();

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

    /**
     * このTileEntityが描画されるべき範囲
     * @return
     */
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB box = new AxisAlignedBB(pos);
        for (GTSTileEntityTrafficArm arm: this.getJointArms()) {
            box = box.union(new AxisAlignedBB(arm.getPos()));
        }
        return box;
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        this.upJoint = GNBTHelper.getBooleanWithValue(compound, "gts_upjoint", false);
        this.bottomJoint = GNBTHelper.getBooleanWithValue(compound, "gts_bottomjoint", false);

        // 繋がっているアームの一覧は、各配列として参照する
        ArrayList<GTSTileEntityTrafficArm> arms = new ArrayList<>();
        for (int i = 0; i < compound.getTagList("gts_attached_traffic_arm", 10).tagCount(); i++) {
            NBTTagCompound tag = compound.getTagList("gts_attached_traffic_arm", 10).getCompoundTagAt(i);
            GTSTileEntityTrafficArm arm = new GTSTileEntityTrafficArm(null);
            arm.readFromNBT(tag);
            arms.add(arm);
        }
        this.jointArms = arms;
    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("gts_upjoint", this.upJoint);
        compound.setBoolean("gts_bottomjoint", this.bottomJoint);

        // アタッチされたアームはリストにして格納する
        NBTTagList list = new NBTTagList();
        for (GTSTileEntityTrafficArm arm: this.jointArms) {
            NBTTagCompound c = new NBTTagCompound();
            c = arm.writeToNBT(c);
            list.appendTag(c);
        }
        compound.setTag("gts_attached_traffic_arm", list);
        return compound;
    }

    public ArrayList<GTSTileEntityTrafficArm> getJointArms() {
        return jointArms;
    }

    public void setJointArms(ArrayList<GTSTileEntityTrafficArm> jointArms) {
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

    @Override
    public void attach(GTSTileEntityTrafficArm te) {
        if (this.jointArms.contains(te)) return;
        this.jointArms.add(te);
        this.markDirty();
        if (this.world.isRemote) {
            GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficPole.class));
        }
        else {
            world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 15);
        }
    }

    @Override
    public void deattach(GTSTileEntityTrafficArm te) {
        this.jointArms.remove(te);
        this.markDirty();
        if (this.world.isRemote) {
            GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficPole.class));
        }
        else {
            world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 15);
        }
    }

    @Override
    public void reset() {
        this.jointArms.clear();
        this.markDirty();
        if (this.world.isRemote) {
            GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficPole.class));
        }
        else {
            world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 15);
        }
    }
}
