package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * GTSの交通信号制御機において、サイクル1つを表す概念。
 * サイクルについての詳説はここでは割愛するが、基本的には
 * 1つのサイクルに対して複数のフェーズで構成され、シーケンシャルに実行される。
 * ただし、場合によってはシーケンシャルではないこともあるので、この基本クラスでは
 * 多機能に対応できるようにしている。通常はシーケンシャルにしたサイクルを使用する。
 */
public abstract class GTSCycle {

    /**
     * このサイクルの名前。特に決まっていなければランダム。半角英数字。
     */
    protected String id;

    /**
     * このサイクルが持つフェーズの一覧。順番に特に意味はないがシーケンシャルの場合
     * この上から順番に実行されるようになる。
     */
    protected final ArrayList<GTSPhase> phases = new ArrayList<>();

    /**
     * 現在実行中のフェーズ番号。上記phasesと対応する。
     */
    protected int nowPhase = 0;

    /**
     * このサイクルの終了フラグ
     */
    protected boolean end = false;

    /**
     * このサイクルが開始してからの経過Tick。tickメソッドを実行することで更新される。
     */
    protected long tick;

    /**
     * GUIで表示させるフィールド一覧。数が多いと見切れる。内容は全てStringになるので
     * 中身を整数とかで使いたい場合は適宜パースすること。
     */
    protected ArrayList<String> metaInfo = new ArrayList<>();

    /**
     * デフォルトコンストラクタは用意するものの、基本的には名称を指定して欲しい。
     */
    public GTSCycle() {
        this(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()) + RandomStringUtils.randomAlphanumeric(24));
    }

    /**
     * 指定した名称のサイクルインスタンスを作成する。
     * 後からPhaseを挿入することができる。
     * 同時に指定したい時は別のコンストラクタを使用する。
     * @param id このサイクルの名前。制御機単位で固有である必要がある。
     */
    public GTSCycle(String id) {
        this.id = id;
    }

    /**
     * このサイクルを開始できるかどうかを返す。
     * このメソッドは、このサイクルが登録されている制御機によって呼び出される。
     * 通常設置直後、あるいは設置後サイクルが1周した場合に次のサイクルを呼び出す為のチェックとして
     * 行われる。順番にサイクルを調査し、どれか一つでもTrueを返したらそのサイクルを次の実行サイクルとする。
     * 基本的に2つ以上がヒットしないように作るべきで、現状は最も最初に見つかったものとなるが
     * この動作に依存するような処理の書き方はしないこと。
     *
     * このメソッドはサーバーで実行されることが多いが、極まれに（なぜか）クライアントで実行されることもある。
     * そのさい、WorldインスタンスがNULLになっていることがある。
     *
     * @param te 呼び出した制御機の情報。
     * @param detected 検知信号が送信されたかどうか。
     * @param world この制御機が設置されているワールドのインスタンス。天候などを取りたいときに利用できる。NULLになることもある。
     * @return このサイクルが起動可能であればtrue、そうでない場合はfalse
     */
    public abstract boolean canStart(GTSTileEntityTrafficController te, boolean detected, World world);

    /**
     * このサイクルのフェーズが1回終了した場合に、次のフェーズを決めるためのメソッド。
     * nextPhaseメソッドを実行すると呼び出され、今動いているフェーズやワールドの状況をもとに次のフェーズを決められる。
     * 負の数を返すと、サイクルを終了する合図となる。
     * nextPhaseメソッドはfinalのため変えられない。
     *
     * @param te 制御機の情報。
     * @param detected 検知信号が受信されているかどうか。
     * @param world 制御機が設置されているワールドのインスタンス。
     * @return 次に実行すべきフェーズ。
     */
    public abstract int getNextPhase(GTSTileEntityTrafficController te, boolean detected, World world);

    /**
     * resetPhaseで使用する。Phaseの中でサイクルとして一番最初に現示すべきPhaseの番号を返す。
     * オーバーする場合はresetPhaseの中でチェックされるが、シーケンシャルの場合は無難に0を返しておくと良い。
     * @param te 制御機の情報。
     * @param detected 検知信号が受信されているかどうか。
     * @param world 制御機が設置されているワールドのインスタンス。
     * @return 最初に実行すべきフェーズ。
     */
    public abstract int getInitialPhase(GTSTileEntityTrafficController te, boolean detected, World world);

    /**
     * このサイクルを次のフェーズに進める。呼び出し元は制御機を想定している。
     * なお、このメソッドは継承先で変更ができない。変更による誤爆や副作用を避けるため。
     * @param te 制御機の情報。
     * @param detected 検知信号が受信されているかどうか。
     * @param world 制御機が設置されているワールドのインスタンス。
     */
    public final void nextPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        int next = Math.min(this.getNextPhase(te, detected, world), this.phases.size() - 1); // 飛びでないように
        if (next >= 0) {
            this.phases.get(this.nowPhase).resetTick();
            this.nowPhase = next;
            this.phases.get(this.nowPhase).resetTick();
        }
        else {
            this.end = true;
        }
    }

    /**
     * このサイクルをリセットする。呼び出し元は制御機を想定している。
     * なお、このメソッドは継承先で変更ができない。変更による誤爆や副作用を避けるため。
     * @param te 制御機の情報。
     * @param detected 検知信号が受信されているかどうか。
     * @param world 制御機が設置されているワールドのインスタンス。
     */
    public final void resetPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        int initial = Math.min(this.getInitialPhase(te, detected, world), this.phases.size() - 1);
        if (initial < 0) initial = 0;
        this.end = false;
        this.nowPhase = initial;
        this.phases.get(this.nowPhase).resetTick();
        this.tick = 0;
    }

    /**
     * このサイクルを1つ動かす。基本的にこのメソッドを呼ぶことで自動でサイクルの処理が行われるようにしている。
     * 交通信号制御機は各Tickごとにこのメソッドを呼ぶことで、サイクルのステップを実現する。
     *
     * @param te 制御機の情報。
     * @param detected 検知信号が受信されているかどうか。
     * @param world 制御機が設置されているワールドのインスタンス。
     *
     * @return このサイクルを終了する場合はTrue、しない場合はFalse
     */
    public boolean tick(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.phases.isEmpty()) return true; // フェーズが何もない状況では実行しない
        // 現在のフェーズを取得する
        GTSPhase phase = this.getNowPhase();
        if (phase == null) {
            // 普通ないが、NULLの場合はそれ以上行えないのでリセットする
            this.resetPhase(te, detected, world);
            return true;
        }

        // 現在のフェーズが終了条件かどうかを判断する
        if (phase.shouldContinue(te, this.tick, detected, world)) {
            // まだ継続する場合
            phase.nextTick();
            this.tick++;
            return false;
        }
        else {
            // フェーズの終了の場合、次のフェーズへ移行する
            this.nextPhase(te, detected, world);

            if (this.end) {
                // 終了の場合、リセットを行う
                this.resetPhase(te, detected, world);
                return true;
            }

            return false;
        }
    }

    public String getId() {
        return id;
    }

    /**
     * 現在表示しているフェーズの番号を返す。
     * @return フェーズ番号
     */
    public int getNowPhaseNumber() {
        return nowPhase;
    }

    /**
     * 現在表示しているフェーズの番号を返す。
     * set等のときに飛び越えないようにしているが、万一を想定して一応ラップしている。
     * @return 現在のフェーズ。明らかにおかしい場合はnull。
     */
    public GTSPhase getNowPhase() {
        try {
            if (this.nowPhase > this.phases.size()) {
                this.nowPhase = 0; // 辻褄が合わない場合は強制的に戻す
            }
            return phases.get(this.nowPhase);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GTSPhase> getPhases() {
        return this.phases;
    }

    /**
     * このサイクルが終了している場合はTrue
     * @return 終了しているか
     */
    public boolean isEnd() {
        return this.end;
    }

    /**
     * このサイクルを強制的に終了する。通常は使用しない。
     * 既に終了しているサイクルに対して行った場合、何も行われない。
     */
    public void finish() {
        this.end = true;
    }

    @Override
    public String toString() {
        return "GTSCycle{" +
                "end=" + end +
                ", id='" + id + '\'' +
                ", phases=" + phases +
                ", nowPhase=" + nowPhase +
                ", tick=" + tick +
                '}';
    }

    public ArrayList<String> getMetaInfo() {
        return this.metaInfo;
    }

    public void setMetaInfo(ArrayList<String> metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * metainfoの中にある各フィールドに対応したテキストラベル名を表示する。
     * 特にない場合、「meta1」「meta2」……のように割り振られる。
     * オーバーライドする場合は、superで呼び出した戻り値に追加していくこと。
     *
     * なお、順番（インデックス）は一致させないとバグるので注意。
     * @return 各metaInfoに対応するタイトルの文字列。GUIに表示される。
     */
    public ArrayList<String> getMetaInfoTitles() {
        return new ArrayList<>();
    }
}
