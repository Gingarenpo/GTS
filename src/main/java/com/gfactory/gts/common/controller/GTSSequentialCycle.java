package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;

/**
 * Phaseの中を登録順に上から順番に実行していくシンプルなシーケンシャルサイクル。
 * ただし、これそのものも抽象クラスであり、実際はcanStartのみ実装をしていない。
 * ここは可変のため、定型クラスとしては保持しない。
 */
public abstract class GTSSequentialCycle extends GTSCycle {

    @Override
    public int getNextPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.nowPhase == this.phases.size() - 1) {
            return -1; // 最後まで行ったら終了
        }
        else {
            return ++this.nowPhase;
        }
    }

    @Override
    public int getInitialPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        return 0;
    }
}
