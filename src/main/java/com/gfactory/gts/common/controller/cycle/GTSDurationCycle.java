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
        long current = this.getExtendedWorldTime(world, this.startTick, this.endTick);
        return this.startTick <= current && current < this.endTick;
    }

    /**
     * 翌日にまたぐ期間のことを考慮して、0-48000で拡張された時間を返す。
     *
     * @param world 世界
     * @param startTick 基準となる最初の時刻
     * @param endTick 基準となる最後の時刻
     * @return 原則0-24000だが、durationの関係上翌日の時刻も必要な場合は翌日を24000～48000とみなしその値として返す
     */
    protected long getExtendedWorldTime(World world, long startTick, long endTick) {
        long time = world.getWorldTime() % 24000;
        if (endTick > 24000 && time < (endTick - 24000)) {
            return time + 24000;
        }
        return time;
    }
}
