package com.gfactory.gts.common.controller.cycle;

import com.gfactory.gts.common.controller.phase.GTSDetectionWaitingPhase;
import com.gfactory.gts.common.controller.phase.GTSPhase;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;

/**
 * 押ボタンや感知器など、検知信号を送信できるものを用いたサイクル。
 * 検知信号を受信するまでは初期状態のフェーズを表示し、
 * 検知信号を受信後はシーケンスに回す。待機状態のdetectedは一番最初のフェーズに登録する。
 * オプションとして「感知する時間帯」を決めることができる。これにより夜間感応式などの再現が可能。
 * この時間帯を外れる場合は通常通りフェーズが進行していく。なお、その場合最初のフェーズは
 * 強制的にdetected扱いになるので注意すること。オプションで、detectedにするまでのTickを
 * 選択でき、その場合はこの指定値後に強制的にDetectedにする。
 * GTSDurationCycleを継承しているので期間の指定も可能！
 */
public class GTSDetectedCycle extends GTSDurationCycle {

    /**
     * 感知時間帯開始Tick。デフォルトは0時
     */
    private int detectStartTick = 0;

    /**
     * 感知時間帯終了Tick。デフォルトは翌朝0時で、要はずっと感知式
     */
    private int detectEndTick = 24000;

    /**
     * 強制的にdetectedにするTick。感知時間帯に属さない場合、指定した時間で強制的にdetectedにする。感知時間帯は無視される。
     */
    private long forceDetectTick = 0;


    @Override
    public int getInitialPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        return super.getInitialPhase(te, detected, world);
    }

    @Override
    public boolean tick(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.nowPhase == 0 && !(this.detectEndTick > world.getWorldTime() && world.getWorldTime() >= this.detectStartTick)) {
            // 感知時間帯以外の場合でinitialフェーズの場合
            if (this.forceDetectTick < this.tick) {
                // さらに、現在のサイクル待ちが最低待機時間を超えた場合
                te.setDetected(true); // 問答無用で検知信号オンにする
                te.sendDetected();
            }
        }

        return super.tick(te, detected, world);
    }

    @Override
    public void onDetect(GTSTileEntityTrafficController te, World world) {
        System.out.println("呼ばれました");
        if (this.nowPhase == 0 ) {
            GTSPhase phase = this.phases.get(this.nowPhase);
            if (phase instanceof GTSDetectionWaitingPhase) {
                ((GTSDetectionWaitingPhase) phase).detect();
            }
        }
    }

    @Override
    public String toString() {
        return "GTSDetectedCycle{" +
                "tick=" + tick +
                ", phases=" + phases +
                ", nowPhase=" + nowPhase +
                ", metaInfo=" + metaInfo +
                ", id='" + id + '\'' +
                ", end=" + end +
                ", startTick=" + startTick +
                ", endTick=" + endTick +
                ", forceDetectTick=" + forceDetectTick +
                ", detectStartTick=" + detectStartTick +
                ", detectEndTick=" + detectEndTick +
                '}';
    }
}
