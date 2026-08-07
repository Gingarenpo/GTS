package com.gfactory.gts.common.controller.cycle;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;

/**
 * 特定の時間に実行されるサイクルを表す。
 * 必須のフィールドとしてstartTickとendTickがあり、それぞれMinecraft内の
 * 1日の経過Tickで指定する。これが範囲となる。デフォルトは「全範囲」となるが、
 * その場合はGTSFixCycleと全く変わらない。
 *
 */
public class GTSDurationCycle extends GTSSequentialCycle {

    protected int startTick = 0;

    protected int endTick = 24000;

    /**
     * 指定された時間内であれば起動する。
     * @param te 呼び出した制御機の情報。
     * @param detected 検知信号が送信されたかどうか。
     * @param world この制御機が設置されているワールドのインスタンス。天候などを取りたいときに利用できる。NULLになることもある。
     * @return
     */
    @Override
    public boolean canStart(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (world == null) return false;
        return this.startTick <= world.getWorldTime() && world.getWorldTime() < this.endTick;
    }
}
