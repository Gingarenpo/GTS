package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;

/**
 * 指定した時間が経過するまではそのフェーズを保つ一定時間のフェーズ
 */
public class GTSFixPhase extends GTSPhase {

    /**
     * 待つ秒数
     */
    private int stillTicks;

    public GTSFixPhase() {}

    /**
     * 指定したTick数だけ待って終了するフェーズを追加する。
     * @param stillTicks 待つTick数
     */
    public GTSFixPhase(String id, int stillTicks) {
        super(id);
        this.stillTicks = stillTicks;
    }

    @Override
    public boolean shouldContinue(GTSTileEntityTrafficController te, long totalticks, boolean detected, World world) {
        return this.ticks <= stillTicks;
    }
}
