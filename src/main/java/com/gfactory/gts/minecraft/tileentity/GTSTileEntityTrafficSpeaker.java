package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.common.GTSI18n;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.proxy.GTSClientProxy;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import com.gfactory.gts.pack.config.GTSTrafficSpeakerConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;

public class GTSTileEntityTrafficSpeaker extends GTSTileEntity<GTSTrafficSpeakerConfig> implements IGTSAttachable<GTSTileEntityTrafficController> {

    /**
     * このスピーカーがアタッチされている制御機の座標
     */
    private BlockPos attachedTrafficController;

    /**
     * このスピーカーが現在鳴らしているサウンド。何も鳴らしていない場合はNULLになる。
     */
    private String playingSoundKey;

    /**
     * このスピーカーのチャンネル。
     */
    private String channel;

    public GTSTileEntityTrafficSpeaker() {
        this.setDummy();

    }

    /**
     * このスピーカーが現在再生を行っているかどうかを返す。
     * @return 再生中の場合はtrue
     */
    public boolean isPlaying() {
        return this.playingSoundKey != null;
    }

    public void playSound(GTSTrafficLightConfig.GTSTrafficLightPattern pattern) {
        // objects野中で最初に見つかったものを流す
        for (String obj: pattern.getObjects()) {
            if (this.getConfig().getSounds().containsKey(obj)) {
                this.playingSoundKey = obj;
                this.markDirty();
                if (!world.isRemote) {
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
                break;
            }
        }
        this.markDirty();

    }

    public void stopSound() {
        this.playingSoundKey = null;
        this.markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public String getName() {
        return GTSI18n.i18n("tile.traffic_speaker.name");
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
        GTSTrafficSpeakerConfig config = new GTSTrafficSpeakerConfig();
        config.setDummy();
        this.setConfig(config);
        this.channel = "test";
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        // 整数化された座標からBlockPosを復元
        if (compound.hasKey("gts_attached_traffic_controller")) {
            long pos = compound.getLong("gts_attached_traffic_controller");
            this.attachedTrafficController = BlockPos.fromLong(pos);
        }
        // 流れているサウンドキー
        if (compound.hasKey("gts_playing_sound")) {
            this.playingSoundKey = compound.getString("gts_playing_sound");
        }
    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        // アタッチされている制御機の座標を書き出し
        if (this.attachedTrafficController != null) {
            long pos = this.attachedTrafficController.toLong();
            compound.setLong("gts_attached_traffic_controller", pos);
        }
        else if (compound.hasKey("gts_attached_traffic_controller")) {
            compound.removeTag("gts_attached_traffic_controller");
        }

        // 流れているサウンドキー
        if (this.playingSoundKey != null) {
            compound.setString("gts_playing_sound", this.playingSoundKey);
        }
        else {
            compound.removeTag("gts_playing_sound");
        }
        return compound;
    }

    @Override
    public void attach(GTSTileEntityTrafficController te) {
        this.attachedTrafficController = te.getPos();
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficButton.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 15);
    }

    @Override
    public void deattach(GTSTileEntityTrafficController te) {
        this.attachedTrafficController = null;
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficButton.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 15);
    }

    @Override
    public void reset() {
        this.attachedTrafficController = null;
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficButton.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 15);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        // クライアント側の場合、パケットを受け取った瞬間にクライアントマネージャーを更新する
        if (world.isRemote) {
            GTSClientProxy.CLIENT_SOUND_MANAGER.updateState(this.pos, this.pack, this.pack.getSoundLocations().get(this.getConfig().getSounds().get(this.playingSoundKey)), true);
        }
    }

    /**
     * TileEntityが消滅するときに呼ばれる。
     * 強制的にサウンドを停止する（BreakBlockだと間に合わないようなので）
     * 制御機のアタッチ解除はBreakBlockでやる
     */
    @Override
    public void invalidate() {
        super.invalidate();

        if (world != null && world.isRemote) {
            GTSClientProxy.CLIENT_SOUND_MANAGER.updateState(pos, null, null, false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.world != null && this.world.isRemote) {
            GTSClientProxy.CLIENT_SOUND_MANAGER.updateState(this.pos, this.pack, this.pack.getSoundLocations().get(this.getConfig().getSounds().get(this.playingSoundKey)), true);
        }
    }

    /**
     * このスピーカーが他の制御機にアタッチしているかを返す。
     * @return アタッチしていればtrue
     */
    public boolean isAttached() {
        return this.attachedTrafficController != null;
    }

    public BlockPos getAttachedTrafficController() {
        return attachedTrafficController;
    }
}
