package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 定周期を表すサイクルの例。組み込みのスタイルということで指定してある。
 * メタデータとして以下を持つ。
 * <ol start="0">
 *     <li>主道の青時間</li>
 *     <li>主道歩灯点滅時間</li>
 *     <li>側道の青時間</li>
 *     <li>側道歩灯点滅時間</li>
 *     <li>黄色時間</li>
 *     <li>クリアランスタイム（歩灯点滅後の待ち時間、全赤時間）</li>
 * </ol>
 * それぞれTickで入力することで、完全定周期を実現する。
 * なお、どんな状態であっても無条件でこのサイクルは実行可能となるため、
 * 夜間点滅などには向かない
 */
public class GTSFixCycle extends GTSSequentialCycle {

    /**
     * JSON作成用に用意しているものである。通常使用しないこと。
     */
    public GTSFixCycle() {}

    /**
     * 4つのチャンネルと6つの現示を元に、完全定周期のサイクルを作成する。
     * 各信号機は同じパターンを持って動くものとする。カスタムしたい場合は、
     * 自前でクラスを作成すること。
     * これはあくまで例！
     * @param mainCarChannel 主道車灯チャンネル名
     * @param subCarChannel 従道車灯チャンネル名
     * @param mainPedChannel 主道歩灯チャンネル名
     * @param subPedChannel 従道歩灯チャンネル名
     * @param carGreen 車灯青現示
     * @param carYellow 車灯黄現示
     * @param carRed 車灯赤現示
     * @param pedGreen 歩灯青現示
     * @param pedFlush 歩灯点滅現示
     * @param pedRed 歩灯赤現示
     */
    public GTSFixCycle(
            String mainCarChannel,
            String subCarChannel,
            String mainPedChannel,
            String subPedChannel,
            GTSTrafficLightConfig.GTSTrafficLightPattern carGreen,
            GTSTrafficLightConfig.GTSTrafficLightPattern carYellow,
            GTSTrafficLightConfig.GTSTrafficLightPattern carRed,
            GTSTrafficLightConfig.GTSTrafficLightPattern pedGreen,
            GTSTrafficLightConfig.GTSTrafficLightPattern pedFlush,
            GTSTrafficLightConfig.GTSTrafficLightPattern pedRed
            ) {
        // メタ情報の初期化
        this.metaInfo = new ArrayList<>();
        this.metaInfo.addAll(Arrays.asList("100", "80", "100", "60", "60", "40"));
        // 第1フェーズ：主道青、側道赤
        GTSPhase phase1 = new GTSFixPhase("phase1", Integer.parseInt(this.metaInfo.get(0)));
        phase1.getChannels().put(mainCarChannel, carGreen);
        phase1.getChannels().put(subCarChannel, carRed);
        phase1.getChannels().put(mainPedChannel, pedGreen);
        phase1.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase1);

        // 第2フェーズ：主道青点滅、側道赤
        GTSPhase phase2 = new GTSFixPhase("phase2", Integer.parseInt(this.metaInfo.get(1)));
        phase2.getChannels().put(mainCarChannel, carGreen);
        phase2.getChannels().put(subCarChannel, carRed);
        phase2.getChannels().put(mainPedChannel, pedFlush);
        phase2.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase2);

        // 第3フェーズ：主道歩灯赤、側道赤
        GTSPhase phase3 = new GTSFixPhase("phase3", Integer.parseInt(this.metaInfo.get(5)));
        phase3.getChannels().put(mainCarChannel, carGreen);
        phase3.getChannels().put(subCarChannel, carRed);
        phase3.getChannels().put(mainPedChannel, pedRed);
        phase3.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase3);

        // 第4フェーズ：主道黄色、側道赤
        GTSPhase phase4 = new GTSFixPhase("phase4", Integer.parseInt(this.metaInfo.get(4)));
        phase4.getChannels().put(mainCarChannel, carYellow);
        phase4.getChannels().put(subCarChannel, carRed);
        phase4.getChannels().put(mainPedChannel, pedRed);
        phase4.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase4);

        // 第5フェーズ：主道赤、側道赤
        GTSPhase phase5 = new GTSFixPhase("phase5", Integer.parseInt(this.metaInfo.get(5)));
        phase5.getChannels().put(mainCarChannel, carRed);
        phase5.getChannels().put(subCarChannel, carRed);
        phase5.getChannels().put(mainPedChannel, pedRed);
        phase5.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase5);

        // 第6フェーズ：主道赤、側道青
        GTSPhase phase6 = new GTSFixPhase("phase6", Integer.parseInt(this.metaInfo.get(2)));
        phase6.getChannels().put(mainCarChannel, carRed);
        phase6.getChannels().put(subCarChannel, carGreen);
        phase6.getChannels().put(mainPedChannel, pedRed);
        phase6.getChannels().put(subPedChannel, pedGreen);
        this.phases.add(phase6);

        // 第7フェーズ：主道赤、側道青点滅
        GTSPhase phase7 = new GTSFixPhase("phase7", Integer.parseInt(this.metaInfo.get(3)));
        phase7.getChannels().put(mainCarChannel, carRed);
        phase7.getChannels().put(subCarChannel, carGreen);
        phase7.getChannels().put(mainPedChannel, pedRed);
        phase7.getChannels().put(subPedChannel, pedFlush);
        this.phases.add(phase7);

        // 第8フェーズ：主道赤、側道青点滅
        GTSPhase phase8 = new GTSFixPhase("phase7", Integer.parseInt(this.metaInfo.get(3)));
        phase8.getChannels().put(mainCarChannel, carRed);
        phase8.getChannels().put(subCarChannel, carGreen);
        phase8.getChannels().put(mainPedChannel, pedRed);
        phase8.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase8);

        // 第9フェーズ：主道赤、側道青点滅
        GTSPhase phase9 = new GTSFixPhase("phase9", Integer.parseInt(this.metaInfo.get(5)));
        phase9.getChannels().put(mainCarChannel, carRed);
        phase9.getChannels().put(subCarChannel, carGreen);
        phase9.getChannels().put(mainPedChannel, pedRed);
        phase9.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase9);

        // 第10フェーズ：主道赤、側道青点滅
        GTSPhase phase10 = new GTSFixPhase("phase10", Integer.parseInt(this.metaInfo.get(4)));
        phase10.getChannels().put(mainCarChannel, carRed);
        phase10.getChannels().put(subCarChannel, carYellow);
        phase10.getChannels().put(mainPedChannel, pedRed);
        phase10.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase10);

        // 第11フェーズ：主道赤、側道赤
        GTSPhase phase11 = new GTSFixPhase("phase11", Integer.parseInt(this.metaInfo.get(5)));
        phase11.getChannels().put(mainCarChannel, carRed);
        phase11.getChannels().put(subCarChannel, carRed);
        phase11.getChannels().put(mainPedChannel, pedRed);
        phase11.getChannels().put(subPedChannel, pedRed);
        this.phases.add(phase11);
    }

    @Override
    public boolean canStart(GTSTileEntityTrafficController te, boolean detected, World world) {
        return true;
    }

    @Override
    public ArrayList<String> getMetaInfoTitles() {
        ArrayList<String> result = super.getMetaInfoTitles();
        result.add(I18n.format("gts.cycle.fix.arg1"));
        result.add(I18n.format("gts.cycle.fix.arg2"));
        result.add(I18n.format("gts.cycle.fix.arg3"));
        result.add(I18n.format("gts.cycle.fix.arg4"));
        result.add(I18n.format("gts.cycle.fix.arg5"));
        result.add(I18n.format("gts.cycle.fix.arg6"));
        return result;
    }
}
