package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.sound.GTSSoundTrafficButtonBase;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficButtonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

/**
 * 押ボタン箱のTileEntity。
 * 押ボタン箱は、1つの制御機をアタッチする。
 * 押されると、検知信号を制御機に与える。そして、検知すると検知後のテクスチャに置き換わる。
 * 最大待機時間を設定でき、これを設定するとこの値を超過した場合問答無用で再度押せるようにする。
 * 二度と押せなくなるのを防ぐため。
 *
 * なお、押ボタン箱ではあるが一応音響も兼ねている（常時なる音、見たいな）
 */
public class GTSTileEntityTrafficButton extends GTSTileEntity<GTSTrafficButtonConfig> implements IGTSAttachable<GTSTileEntityTrafficController>, ITickable {

    /**
     * アタッチされている制御機の座標
     */
    private BlockPos attachedTrafficController;

    /**
     * 検知信号を送信したかどうか。テクスチャの状況を見る
     */
    private boolean detected;

    /**
     * アタッチされていない場合や、制御機がうんともすんとも言わない場合に
     * 強制的に検知状態を解除する為の待機Tick数。デフォルトは0で、
     * 無制限に待機する。制御機が検知信号を適切に解除しない限り、
     * 永遠に押せなくなる。なおアタッチされていない場合はそもそも押せない。
     */
    private int maxWaitTick = 0;

    /**
     * detectedになってからの経過Tick
     */
    private int nowTick = 0;


    public GTSTileEntityTrafficButton() {
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
        GTSTrafficButtonConfig config = new GTSTrafficButtonConfig();
        config.setDummy();
        this.setConfig(config);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        // アタッチされた制御機の情報
        if (compound.hasKey("gts_attached_traffic_controller")) {
            this.attachedTrafficController = BlockPos.fromLong(compound.getLong("gts_attached_traffic_controller"));
        }
        // その他プリミティブ
        this.detected = compound.getBoolean("gts_detected");
        this.maxWaitTick = compound.getInteger("gts_maxWaitTick");
        this.nowTick = compound.getInteger("gts_nowTick");

    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        // プリミティブ系
        compound.setBoolean("gts_detected", this.detected);
        compound.setInteger("gts_maxWaitTick", this.maxWaitTick);
        compound.setInteger("gts_nowTick", this.nowTick);

        // アタッチされた制御機がある場合はその制御機を入れる
        if (this.attachedTrafficController != null) {
            compound.setLong("gts_attached_traffic_controller", this.attachedTrafficController.toLong());
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

    public BlockPos getAttachedTrafficController() {
        return attachedTrafficController;
    }

    public void setAttachedTrafficController(BlockPos attachedTrafficController) {
        this.attachedTrafficController = attachedTrafficController;
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        if (detected && !this.detected) this.nowTick = 0; // 検知を初めて行った際は初期化
        this.detected = detected;
        this.sendDetected();

    }

    public int getMaxWaitTick() {
        return maxWaitTick;
    }

    public void setMaxWaitTick(int maxWaitTick) {
        this.maxWaitTick = maxWaitTick;
    }

    /**
     * Tickごとに呼び出される。
     * ここで最大待機時間と比較し、経過したらdetectedをfalseに強制的に戻す。
     */
    @Override
    public void update() {
        if (this.maxWaitTick > 0 && this.nowTick >= this.maxWaitTick) {
            this.nowTick = 0;
            this.detected = false;
        }
        else if (this.nowTick >= Integer.MAX_VALUE - 5) {
            this.nowTick = 0; // 負の数にオーバーフローすることを避けるため
        }
        else {
            this.nowTick++;
        }

        // 常時鳴らす音の判定で、鳴らす
        if (this.world.isRemote && this.getConfig() != null && this.getConfig().getBaseSoundTick() != 0 && world.getTotalWorldTime() % this.getConfig().getBaseSoundTick() == 0) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new GTSSoundTrafficButtonBase(this));
        }
    }

    /**
     * アタッチされた制御機に対して検知信号を更新するメソッド。
     * setDetectedを使用すると呼ばれる
     */
    private void sendDetected() {
        if (this.attachedTrafficController == null) return;

        // 制御機のTileEntityを取得
        TileEntity te = this.world.getTileEntity(this.attachedTrafficController);
        if (!(te instanceof GTSTileEntityTrafficController)) return;
        GTSTileEntityTrafficController controller = (GTSTileEntityTrafficController) te;

        // 制御機の検知信号を更新
        controller.setDetected(true);
    }
}
