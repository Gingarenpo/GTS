package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

/**
 * GTSの交通信号制御機において使用する、フェーズに関するクラス。
 * フェーズについての詳説は別にまとめるとして、簡単に言うと「あるサイクルのあるタイミングで
 * 現示すべき状態を格納したリスト」って感じ。
 *
 * フェーズはチャンネルを持ち、チャンネルはHashMapによりアクセスする。
 * チャンネルIDとして固有の文字列を指定可能。半角英数字。
 */
public class GTSPhase {

    /**
     * このフェーズの固有のID。半角英数字で、特に指定がない場合はランダムに決まる。
     */
    protected String id;

    /**
     * このフェーズによって切り替えられるチャンネルの情報。いくつでも追加できる。
     * チャンネルの出し入れはこのフェーズクラスから行う。
     */
    protected final LinkedHashMap<String, GTSTrafficLightConfig.GTSTrafficLightPattern> channels = new LinkedHashMap<>();

    /**
     * このフェーズがスタートしてから経過したTick数。longなので事実上無制限。
     */
    protected long ticks;

    /**
     * デフォルトコンストラクタは、何もかもが初期値、IDランダムの物を作成する。
     * GUIからの操作用。
     */
    public GTSPhase() {
        this.id = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()) + RandomStringUtils.randomAlphanumeric(24);
    }

    /**
     * IDを指定して作成。こっちが素。
     * @param id フェーズID。
     */
    public GTSPhase(String id) {
        this.id = id;
    }

    /**
     * このフェーズにチャンネルを追加する。既に指定されたidが存在する場合、上書きされるので注意。
     * @param id チャンネルID。
     * @param pattern チャンネルパターン。
     * @return 追加した後の自分自身。
     */
    public GTSPhase addChannel(String id, GTSTrafficLightConfig.GTSTrafficLightPattern pattern) {
        this.channels.put(id, pattern);
        return this;
    }

    /**
     * このフェーズにチャンネルを追加する。既に指定されたidが存在する場合、例外を出す。
     * @param id チャンネルID。
     * @param pattern チャンネルパターン。
     * @return 追加した後の自分自身。
     * @throws IllegalArgumentException 既にデータが存在する場合。
     */
    public GTSPhase addChannelTry(String id, GTSTrafficLightConfig.GTSTrafficLightPattern pattern) throws IllegalArgumentException {
        if (this.channels.containsKey(id)) {
            throw new IllegalArgumentException("Key" + id + " exist!");
        }
        return this.addChannel(id, pattern);
    }

    public long getTicks() {
        return this.ticks;
    }

    /**
     * フェーズの経過時間をリセットする。
     * @return リセットした自分自身。
     */
    public GTSPhase resetTick() {
        this.ticks = 0;
        return this;
    }

    /**
     * 経過時間を1つ進める。tickごとの呼び出しで使う。
     * @return 進めた後の自分自身。
     */
    public GTSPhase nextTick() {
        this.ticks++;
        return this;
    }

    /**
     * 指定したTicksに強制的に変換する。通常は使用しない。整合性が取れなくなったときに使用する。
     * @param ticks 設定したいTicks
     * @return 設定後の自分自身。
     */
    public GTSPhase setTicks(long ticks) {
        this.ticks = ticks;
        return this;
    }

    /**
     * このフェーズの全チャンネルを返す。GUIで使うことを想定している。
     * @return 全チャンネル
     */
    public LinkedHashMap<String, GTSTrafficLightConfig.GTSTrafficLightPattern> getChannels() {
        return this.channels;
    }

    /**
     * Tickごとに呼び出されることを想定しており、このフェーズが切り替わる条件を記す。
     * このメソッドがfalseを返した場合、このフェーズは終了とみなし次のフェーズに進む。
     * Tickごとの呼び出しとなるため、ここで不要な処理を記述するとゲーム全体のレスポンスに影響が出る。
     *
     * @param te このフェーズが登録されている制御機のTileEntity。
     * @param totalticks サイクルが始まってからの経過Tick。このフェーズより前にフェーズが起動していた場合は加算されている。
     * @param detected 検知信号が受信されたかどうか。
     * @param world 制御機が設置されている場所のWorld。
     * @return このフェーズを続行するべきならTrue、終了するべきならFalse。
     */
    public boolean shouldContinue(GTSTileEntityTrafficController te, long totalticks, boolean detected, World world) {
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GTSPhase{" +
                "channels=" + channels +
                ", id='" + id + '\'' +
                ", ticks=" + ticks +
                '}';
    }
}
