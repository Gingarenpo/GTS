package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.common.controller.GTSCycle;
import com.gfactory.gts.common.controller.GTSFixCycle;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficControllerConfig;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * 交通信号制御機に関するデータを保持するTileEntity。
 * 従来はDataとして分離していたが、TileEntityで持たせてしまった方が効率がいいので
 * こちらで保持を行う。
 *
 * サイクルのデータや、フェーズのデータはシリアライズしてJSON形式の文字列にして渡す想定。
 */
public class GTSTileEntityTrafficController extends GTSTileEntity<GTSTrafficControllerConfig> implements ITickable, IGTSAttachable<GTSTileEntity> {

    /**
     * この交通信号制御機にアタッチされたサイクルの一覧。
     */
    private ArrayList<GTSCycle> cycles = new ArrayList<>();

    /**
     * 現在のサイクルの番号。ArrayListのインデックスと一致する。
     */
    private int nowCycle = 0;

    /**
     * この制御機が何かしらの検知信号を受信しているかどうか。
     */
    private boolean detected = false;

    /**
     * この制御機がアタッチされている交通信号機の一覧。
     */
    private ArrayList<BlockPos> attachedTrafficLights = new ArrayList<>();

    /**
     * この制御機がアタッチされている交通信号制御機の一覧。
     */
    private ArrayList<BlockPos> attachedTrafficButtons = new ArrayList<>();


    public GTSTileEntityTrafficController() {
        this.setDummy();
    }

    @Override
    public String getName() {
        return I18n.format("tile.traffic_controller.name");
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
        GTSTrafficControllerConfig config = new GTSTrafficControllerConfig();
        config.setDummy();
        this.setConfig(config);

        // TODO: 消す
        GTSTrafficLightConfig.GTSTrafficLightPattern carGreen = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        carGreen.getObjects().add("g300");
        carGreen.getObjects().add("g250");
        GTSTrafficLightConfig.GTSTrafficLightPattern carYellow = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        carYellow.getObjects().add("y300");
        carYellow.getObjects().add("y250");
        GTSTrafficLightConfig.GTSTrafficLightPattern carRed = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        carRed.getObjects().add("r300");
        carRed.getObjects().add("r250");
        GTSTrafficLightConfig.GTSTrafficLightPattern pedGreen = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        pedGreen.getObjects().add("g");
        GTSTrafficLightConfig.GTSTrafficLightPattern pedFlush = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        pedFlush.getObjects().add("g");
        pedFlush.setTick(10);
        GTSTrafficLightConfig.GTSTrafficLightPattern pedRed = new GTSTrafficLightConfig.GTSTrafficLightPattern();
        pedRed.getObjects().add("r");
        this.cycles.add(new GTSFixCycle("mainCar", "subCar", "mainPed", "subPed", carGreen, carYellow, carRed, pedGreen, pedFlush, pedRed));
    }



    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        // 制御機固有の情報（プリミティブ）
        this.detected = compound.getBoolean("gts_detected");
        this.nowCycle = compound.getInteger("gts_nowCycle");

        // アタッチ済みの座標はリスト形式で読み込む
        ArrayList<BlockPos> pos = new ArrayList<>();
        for (int i = 0; i < compound.getTagList("gts_attached_traffic_light", 10).tagCount(); i++) {
            NBTTagCompound tag =compound.getTagList("gts_attached_traffic_light", 10).getCompoundTagAt(i);
            pos.add(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
        }
        this.attachedTrafficLights = pos;

        pos = new ArrayList<>();
        for (int i = 0; i < compound.getTagList("gts_attached_traffic_button", 10).tagCount(); i++) {
            NBTTagCompound tag =compound.getTagList("gts_attached_traffic_button", 10).getCompoundTagAt(i);
            pos.add(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
        }
        this.attachedTrafficButtons = pos;

        // CyclesはJSONでシリアライズ等を行う
        // ちょっと回りくどいやり方しているけどこうするしかない
        if (compound.hasKey("gts_cycles")) {
            Type listType = new TypeToken<ArrayList<GTSCycle>>() {}.getType();
            this.cycles = GTS.GSON.fromJson(compound.getString("gts_cycles"), listType);
        }


    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        // プリミティブの書き込み
        compound.setBoolean("gts_detected", this.detected);
        compound.setInteger("gts_nowCycle", this.nowCycle);

        // アタッチされた座標はリストにして格納する
        NBTTagList list = new NBTTagList();
        for (BlockPos pos: this.attachedTrafficLights) {
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("x", pos.getX());
            c.setInteger("y", pos.getY());
            c.setInteger("z", pos.getZ());
            list.appendTag(c);
        }
        compound.setTag("gts_attached_traffic_light", list);
        list = new NBTTagList();
        for (BlockPos pos: this.attachedTrafficButtons) {
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("x", pos.getX());
            c.setInteger("y", pos.getY());
            c.setInteger("z", pos.getZ());
            list.appendTag(c);
        }
        compound.setTag("gts_attached_traffic_button", list);

        // Cyclesはそれを全てJSONにして
        Type listType = new TypeToken<ArrayList<GTSCycle>>() {}.getType();
        compound.setString("gts_cycles", GTS.GSON.toJson(this.cycles, listType));
        return compound;
    }

    /**
     * 1Tickごとに呼び出され、実行される。
     * ここで制御機のサイクル更新を行う。
     */
    @Override
    public void update() {
        if (this.cycles.isEmpty()) return; // サイクルがない状態では何もしない
        GTSCycle cycle = this.cycles.get(this.nowCycle);
        boolean finish = cycle.tick(this, this.detected, this.world);
        if (finish) {
            // 終了する場合、次のサイクルを決定する
            for (int i = 0; i < this.cycles.size(); i++) {
                if (this.cycles.get(i).canStart(this, this.detected, this.world)) {
                    // 次のサイクルを決定
                    this.nowCycle = i;
                    return;
                }
            }
            // 実行可能なサイクルがない場合、判定を次に持ち越す（普通こんなことないようにする）
        }
    }

    public ArrayList<GTSCycle> getCycles() {
        return cycles;
    }

    public boolean isDetected() {
        return detected;
    }

    public int getNowCycleNumber() {
        return nowCycle;
    }

    /**
     * 現在動いているサイクルを返す。動いていない場合はnull
     * @return サイクル
     */
    public GTSCycle getNowCycle() {
        return (this.nowCycle >= 0 && this.nowCycle < this.cycles.size()) ? this.cycles.get(this.nowCycle) : null;
    }

    public void setCycles(ArrayList<GTSCycle> cycles) {
        this.cycles = cycles;
    }

    /**
     * 検知信号を強制的に変更する。外部からの干渉で使用する。
     * @param detected 検知信号のオンオフ。
     */
    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    @Override
    public void attach(GTSTileEntity te) {
        // ある場合は追加しない
        if (te instanceof GTSTileEntityTrafficLight) {
            if (!this.attachedTrafficLights.contains(te.getPos())) {
                this.attachedTrafficLights.add(te.getPos());
            }
        }
        else if (te instanceof GTSTileEntityTrafficButton) {
            if (!this.attachedTrafficButtons.contains(te.getPos())) {
                this.attachedTrafficButtons.add(te.getPos());
                // もし自分自身が検知状態だったら強制的に検知状態にする
                ((GTSTileEntityTrafficButton) te).setDetected(this.detected);
            }
        }
        this.markDirty();
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficController.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public void deattach(GTSTileEntity te) {
        if (te instanceof GTSTileEntityTrafficLight) {
            this.attachedTrafficLights.remove(te.getPos()); // ない場合は何もしないようなので安心して消せる
        }
        else if (te instanceof GTSTileEntityTrafficButton) {
            this.attachedTrafficButtons.remove(te.getPos()); // ない場合は何もしないようなので安心して消せる
        }
        this.markDirty();
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficController.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public void reset() {
        this.attachedTrafficLights = new ArrayList<>();
        this.attachedTrafficButtons = new ArrayList<>();
        if (world.isRemote) GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.writeToNBT(new NBTTagCompound()), this.pos, GTSTileEntityTrafficController.class));
        if (!world.isRemote) world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    public ArrayList<BlockPos> getAttachedTrafficLights() {
        return attachedTrafficLights;
    }

    public void setAttachedTrafficLights(ArrayList<BlockPos> attachedTrafficLights) {
        this.attachedTrafficLights = attachedTrafficLights;
    }

    public ArrayList<BlockPos> getAttachedTrafficButtons() {
        return attachedTrafficButtons;
    }

    public void setAttachedTrafficButtons(ArrayList<BlockPos> attachedTrafficButtons) {
        this.attachedTrafficButtons = attachedTrafficButtons;
    }

    @Override
    public String toString() {
        return "GTSTileEntityTrafficController{" +
                "pack=" + pack +
                ", config=" + config +
                ", angle=" + angle +
                ", nowCycle=" + nowCycle +
                ", detected=" + detected +
                ", cycles=" + cycles +
                ", attachedTrafficLights=" + attachedTrafficLights +
                "} " + super.toString();
    }
}
