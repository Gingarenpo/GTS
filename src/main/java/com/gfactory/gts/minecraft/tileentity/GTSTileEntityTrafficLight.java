package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * TileEntityとして各信号機の情報を保持しておく為のもの
 * 設置されたばかりの場合はダミーパックを置くことになっている。
 */
public class GTSTileEntityTrafficLight extends GTSTileEntity<GTSTrafficLightConfig> implements IGTSAttachable<GTSTileEntityTrafficController> {

    /**
     * この交通信号機がアタッチしている制御機。ない場合はnullが格納される。
     */
    private BlockPos attachedTrafficController = null;

    /**
     * この交通信号機の対応しているチャンネル
     */
    private String channel = "test";


    /**
     * 空のTileEntityを作成する。ワールドに最初に設置されたとき、ワールドに初回ログインして
     * サーバーからデータが渡ってくるまでの一瞬の間にここが呼び出されて作成される。
     * ここではダミーパックを読み込むことで対応する。要は一致するものがなければダミーのまま。
     *
     */
    public GTSTileEntityTrafficLight() {
        this.setDummy();
    }

    @Override
    public String getName() {
        return I18n.format("tile.traffic_light.name");
    }

    public void setDummy() {
        // ダミーファイルをセットする
        GTSPack p = GTS.LOADER.getDummy();
        if (p == null) {
            // ダミーパックが使えない場合、致命的なエラーとなる
            // TODO: とりあえず落としているけど本当はなんかリカバリーすべき
            throw new RuntimeException("Dummy Model cannot be loaded.");
        }
        this.pack = p;
        GTSTrafficLightConfig config = new GTSTrafficLightConfig();
        config.setDummy();
        this.setConfig(config);
    }

    /**
     * 個別にSetterを許可すると大惨事が起こるので、同時に変更のみ認める
     * @param pack 変更対象のPack
     * @param config 変更対象のコンフィグ
     */
    public void change(GTSPack pack, GTSTrafficLightConfig config) {
        this.pack = pack;
        this.config = config;
    }

    public GTSTrafficLightConfig getConfig() {
        return this.config;
    }

    public GTSPack getPack() {
        return this.pack;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * サーバーやクライアントでNBTを使用してやり取りを行ったとき、
     * NBTに含まれる内容からTileEntityの内容を復元するときに使用するメソッド。
     * 中身を読み取り、その中身をTileEntityに反映する。
     * @param compound 送られてきたNBTタグ
     */
    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        this.channel = compound.getString("gts_channel");
        if (compound.hasKey("gts_attached_traffic_controller")) {
            this.attachedTrafficController = BlockPos.fromLong(compound.getLong("gts_attached_traffic_controller")); // 制御機は一つだけ
        }
    }

    /**
     * サーバーやクライアントに渡すためのNBTタグを作成する。
     * サーバーやクライアントに渡すのは、パックの名前とパックのコンフィグの名前である。
     * キーがこれだけだと頼りないけどそのための重複を避けるルールである。
     * @param compound 作成するタグ
     * @return
     */
    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        NBTTagCompound result = compound;
        result.setString("gts_channel", this.channel);
        if (this.attachedTrafficController != null) {
            result.setLong("gts_attached_traffic_controller", this.attachedTrafficController.toLong());
        }
        return result;
    }

    @Override
    public void attach(GTSTileEntityTrafficController te) {
        this.attachedTrafficController = te.getPos();
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficLight.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public void deattach(GTSTileEntityTrafficController te) {
        if (te.getPos().equals(this.attachedTrafficController)) {
            this.attachedTrafficController = null;
            if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficLight.class));
            if (!world.isRemote)  world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public void reset() {
        // NOOP
    }

    public BlockPos getAttachedTrafficController() {
        return attachedTrafficController;
    }

    public void setAttachedTrafficController(BlockPos attachedTrafficController) {
        this.attachedTrafficController = attachedTrafficController;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
