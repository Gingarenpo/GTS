package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.helper.GNBTHelper;
import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * 今回GTSで使用するTileEntityはメモリとかの影響を最小限に抑えるため、
 * 必要最低限のものはすべて共通で実装する。
 */
public abstract class GTSTileEntity<T extends GTSConfig> extends TileEntity {

    /**
     * このパックの中身。モデルなどを引っ張り出すのに必要。
     * 通常はダミーデータが入っており、受信するとそのパック名と一致するものを引っ張り出す。
     */
    protected GTSPack pack;

    /**
     * このコンフィグの中身。packとconfigは必ずセットで使用する。
     * 存在しない場合落ちるので注意。
     */
    protected T config;

    /**
     * 置かれている角度。レンダリングに使用する。
     */
    protected double angle;

    /**
     * モデル描画座標のちょっとしたずれ
     */
    protected double posX;
    protected double posY;
    protected double posZ;

    /**
     * 現在描画されているモデルの最小値・最大値
     */
    protected transient double[][] modelMinMax;

    public GTSTileEntity() {
    }

    /**
     * このTileEntityの名前を返す。
     * GUIのレンダリングに使用する。
     *
     * @return このTileEntityの名前
     */
    public abstract String getName();

    /**
     * ダミーファイルをセットする
     */
    public abstract void setDummy();

    /**
     * サーバーやクライアントでNBTを使用してやり取りを行ったとき、
     * NBTに含まれる内容からTileEntityの内容を復元するときに使用するメソッド。
     * 中身を読み取り、その中身をTileEntityに反映する。
     * @param compound 送られてきたNBTタグ
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        // アングルと座標を読み込む
        this.angle = GNBTHelper.getDoubleWithValue(compound, "gts_angle", 0.0f);
        this.posX = GNBTHelper.getDoubleWithValue(compound, "gts_pos_x", 0.0f);
        this.posY = GNBTHelper.getDoubleWithValue(compound, "gts_pos_y", 0.0f);
        this.posZ = GNBTHelper.getDoubleWithValue(compound, "gts_pos_z", 0.0f);

        // パックとコンフィグの「名前」を読み込む
        String packName = compound.getString("gts_pack_name");
        String packConfig = compound.getString("gts_pack_config");

        // これに置き換えるが、存在しない場合はダミーファイルに戻す
        GTSPack p = GTS.LOADER.getPack(packName);
        if (p == null) {
            GTS.LOGGER.warn(I18n.format("gts.warning.nbt_cannot_load", "pack", packName));
            this.setDummy();
        }
        else {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> c: p.getConfigs().entrySet()) {
                T cc = (T) c.getValue();
                if (cc.getId().equals(packConfig)) {
                    // 完全一致したのでここで終了
                    this.pack = p;
                    this.config = cc;
                }
            }
        }
        this.readDataFromNBT(compound);

    }

    /**
     * サーバーやクライアントに渡すためのNBTタグを作成する。
     * サーバーやクライアントに渡すのは、パックの名前とパックのコンフィグの名前である。
     * キーがこれだけだと頼りないけどそのための重複を避けるルールである。
     * @param compound 作成するタグ
     * @return
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = compound;
        if (!(this instanceof GTSTileEntityDummy)) {
            result = super.writeToNBT(compound);
        }
        else if (this.pos != null) {
            result.setInteger("x", this.pos.getX());
            result.setInteger("y", this.pos.getY());
            result.setInteger("z", this.pos.getZ());
        }
        result.setString("gts_pack_name", this.pack.getName());
        result.setString("gts_pack_config", this.config.getId());
        result.setDouble("gts_angle", this.angle);
        result.setDouble("gts_pos_x", this.posX);
        result.setDouble("gts_pos_y", this.posY);
        result.setDouble("gts_pos_z", this.posZ);
        result = this.writeDataToNBT(result);

        return result;
    }

    /**
     * サーバーやクライアントに渡す、アプデタグを要求されたとき。
     * 通常NBTと同じもの渡すのでそれをそのまま渡せばよい。
     * @return 送るべきNBT
     */
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    /**
     * こちらはアプデタグを内包するパケットを返すもの？
     * TileEntityのデータをパケット化するために必要なものらしく、
     * NBTタグをパケットに詰め込んでラップするためのもの。
     * 中身はNBTなのでupdateTagをそのまま送る。
     * @return 更新用に使用するパケット
     */
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    /**
     * サーバーとかクライアントとかからパケットを受け取ったときにどうするか。
     * ここではそのパケットのNBTを読み込み、TileEntityに反映させる。
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    /**
     * readFromNBTで最小限のものを読み込んだ後、継承先のクラスで独自に追加した
     * フィールドに対する読み書きを行ってもらう。
     *
     * @param compound 初期整形済んだcompound
     */
    public abstract void readDataFromNBT(NBTTagCompound compound);

    /**
     * writeToNBTで最小限のものを書き込んだ後、継承先のクラスで独自に追加した
     * フィールドに対する読み書きを行ってもらう。
     *
     * @param compound 初期書き込み済んだcompound
     * @return 書き込み終えたもの
     */
    public abstract NBTTagCompound writeDataToNBT(NBTTagCompound compound);

    public double getAngle() {
        return angle;
    }

    public T getConfig() {
        return config;
    }

    public GTSPack getPack() {
        return pack;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setConfig(T config) {
        this.config = config;
        // コンフィグの中のポジションを反映させる
        this.posX = this.config.getOriginalPosition()[0];
        this.posY = this.config.getOriginalPosition()[1];
        this.posZ = this.config.getOriginalPosition()[2];
        this.markDirty();
        if (this.world != null) this.world.notifyBlockUpdate(this.pos, world.getBlockState(pos), world.getBlockState(pos), 15);
    }

    public void setPack(GTSPack pack) {
        this.pack = pack;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    /**
     * 座標が一致していれば、それは等しいとみなす
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GTSTileEntity && this.pos != null && this.pos.equals(((GTSTileEntity) obj).pos));
    }

    /**
     * このTileEntityがレンダリングされるべき領域を返す
     * TODO: 重い場合は消す
     * @return バウンディングボックス
     */
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB aabb = super.getRenderBoundingBox();
        if (this.pack == null || this.config == null) return aabb;
        MQO model = this.pack.getResizingModels(this.config.getModel(), this.config.getSize());
        for (MQOObject o: model.getObjects()) {
            MQOVertex[] r = o.getBoundingBox();
            aabb.union(new AxisAlignedBB(r[0].getX(), r[0].getY(), r[0].getZ(), r[1].getX(), r[1].getY(), r[1].getZ()));
        }
        return aabb;
    }

    protected void calcModelMinMax() {
        if (this.pack == null || this.config == null) return;
        MQO model = this.pack.getResizingModels(this.config.getModel(), this.config.getSize());
        double[][] result = new double[][] {new double[]{0, 0}, new double[]{0, 0}, new double[]{0, 0}};
        for (MQOObject o: model.getObjects()) {
            double[][] r = o.getAxisMinMax();
            result[0][0] += r[0][0];
            result[1][0] += r[1][0];
            result[2][0] += r[2][0];
            result[0][1] += r[0][1];
            result[1][1] += r[1][1];
            result[2][1] += r[2][1];
        }
        this.modelMinMax = result;
    }

    public double[][] getModelMinMax() {
        if (this.modelMinMax == null) this.calcModelMinMax();
        return this.modelMinMax;
    }

    /**
     * レンダリング可能範囲を強制的に広げる（そうしないと途中で消える）
     * @return
     */
    @Override
    public double getMaxRenderDistanceSquared() {
        return Math.pow(128, 2);
    }
}
