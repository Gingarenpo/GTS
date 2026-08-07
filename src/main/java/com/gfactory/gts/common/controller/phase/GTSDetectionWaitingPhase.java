package com.gfactory.gts.common.controller.phase;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;

/**
 * 押ボタンや感知器など、検知信号が送信されるまで待つタイプのフェーズ。
 * 感知式や押ボタン式の交差点で使用することを想定している。
 * フィールドには、「最低待ち時間」と検知信号が送信されてからの「待ち時間」を
 * 指定することができる。
 * 最低待ち時間は、連続して検知信号が送信されたときに最低限このフェーズが取る時間である。
 * 検知信号が送信されてからの待ち時間は、その名の通りである。即時切替の場合は0とする。
 * なお、両方設定されている場合は最低待ち時間が優先される。
 *
 * <hr>
 * <h2>例</h2>
 * <ul>
 *     <li>最低待ち時間：100Tick</li>
 *     <li>検知信号送信待ち時間: 50Tick</li>
 * </ul>
 * <p>フェーズ開始後10Tickで「検知信号」を受信した場合、検知信号送信待ち時間をウェイトする。しかし、60Tickなので最低待ち時間を下回る。よって最低待ち時間まで+40Tick待機することになり、結果このフェーズは100Tick続く。</p>
 *
 */
public class GTSDetectionWaitingPhase extends GTSPhase {

    /**
     * 最低待ち時間Tick
     */
    private int mustWaitTick = 0;

    /**
     * 検知信号が送信されてからの待ち時間Tick
     */
    private int detectWaitTick = 0;

    /**
     * 検知信号を受信したときのTick。JSONで指定する場合、ここは0を固定で入れる。
     */
    private long detectedTick = 0;

    @Override
    public boolean shouldContinue(GTSTileEntityTrafficController te, long totalticks, boolean detected, World world) {
        if (!detected) return true; // 検知信号が送信されていない場合は永久に繰り返す
        if (this.ticks <= this.mustWaitTick) return true; // 最低待ち時間に満たない場合は継続する
        return this.ticks <= this.detectedTick + this.detectWaitTick; // 検知後待ち時間中は継続する
    }

    /**
     * サイクルから呼ぶためのメソッド。検知信号を受信した場合、その検知信号受信したTickを格納するためのもの。
     * これを呼ばないと待ち時間が無効化されるので注意。
     */
    public void detect() {
        this.detectedTick = this.ticks;
    }

    @Override
    public String toString() {
        return "GTSDetectionWaitingPhase{" +
                "detectedTick=" + detectedTick +
                ", mustWaitTick=" + mustWaitTick +
                ", detectWaitTick=" + detectWaitTick +
                ", channels=" + channels +
                ", id='" + id + '\'' +
                ", ticks=" + ticks +
                '}';
    }

    @Override
    public GTSPhase resetTick() {
        this.detectedTick = 0;
        return super.resetTick();
    }
}
