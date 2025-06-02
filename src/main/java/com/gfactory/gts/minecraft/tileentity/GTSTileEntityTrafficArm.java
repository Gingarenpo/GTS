package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficArmConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * アームのTileEntity。ただし、pos等を用いるものは使えない。
 */
public class GTSTileEntityTrafficArm extends GTSTileEntityDummy<GTSTrafficArmConfig> {

    public GTSTileEntityTrafficArm(BlockPos pos) {
        super(pos);
        this.setDummy();
    }

    @Override
    public String getName() {
        return I18n.format("item.gts.traffic_arm.name");
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
        GTSTrafficArmConfig config = new GTSTrafficArmConfig();
        config.setDummy();
        this.setConfig(config);
    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {

    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        return compound;
    }
}
