package com.gfactory.gts.common.controller;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 試験的要素：JavaScriptを使用して高度なサイクルを組みたい方向け
 * 現在バグが多いのと、これJava8でのみ使用できる機能で将来拡張が難しいので
 * おまけ程度の仕様として。
 */
public class GTSScriptCycle extends GTSCycle {

    /**
     * スクリプトエンジンを保持しておく。これJSONにできないので
     * インスタンスの作成の度に初期化する。
     */
    private transient ScriptEngine engine;

    /**
     * スクリプトの場所。これがない場合はそもそも動作ができない
     */
    private File scriptFile;


    @Override
    public ArrayList<String> getMetaInfoTitles() {
        ArrayList<String> result = super.getMetaInfoTitles();
        result.add(I18n.format("gts.cycle.script.arg1"));
        return result;
    }

    /**
     * JSONから復元するとき用
     */
    public GTSScriptCycle() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("js");
        if (this.engine == null) {
            // エンジンが見つからず初期化できなかった場合
            throw new RuntimeException(I18n.format("gts.exception.script_engine"));
        }
    }

    /**
     * スクリプトファイルを元にこの制御機を作動させる。
     * スクリプトファイルの場所はFileで指定する。
     * @param scriptFile JavaScriptファイル
     */
    public GTSScriptCycle(File scriptFile) {
        this();
        this.scriptFile = scriptFile;
    }

    @Override
    public boolean canStart(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.scriptFile == null) return false; // スクリプトファイルがない状態では起動できない
        if (this.engine == null) return false; // エンジンがない場合も起動できない
        Map<String, Object> map = this.getBindJavaObjects();
        map.put("te", te);
        map.put("detected", detected);
        map.put("world", world);
        SimpleBindings bind = new SimpleBindings(map);
        try {
            Object flag = this.engine.eval("canStart(te, detected, world);", bind);
            if (flag instanceof Boolean) {
                return (boolean) flag;
            }
            else {
                return false;
            }
        } catch (ScriptException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.eval", e.getMessage()));
            return false;
        }

    }

    @Override
    public int getNextPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.scriptFile == null) return -1; // スクリプトファイルがない状態では起動できない
        if (this.engine == null) return -1; // エンジンがない場合も起動できない
        Map<String, Object> map = this.getBindJavaObjects();
        map.put("te", te);
        map.put("detected", detected);
        map.put("world", world);
        SimpleBindings bind = new SimpleBindings(map);
        try {
            Object flag = this.engine.eval("canStart(te, detected, world);", bind);
            if (flag instanceof Integer) {
                return (int) flag;
            }
            else {
                return -1;
            }
        } catch (ScriptException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.eval", e.getMessage()));
            return -1;
        }
    }

    @Override
    public int getInitialPhase(GTSTileEntityTrafficController te, boolean detected, World world) {
        if (this.scriptFile == null) return 0; // スクリプトファイルがない状態では起動できない
        if (this.engine == null) return 0; // エンジンがない場合も起動できない
        Map<String, Object> map = this.getBindJavaObjects();
        map.put("te", te);
        map.put("detected", detected);
        map.put("world", world);
        SimpleBindings bind = new SimpleBindings(map);
        try {
            Object flag = this.engine.eval("canStart(te, detected, world);", bind);
            if (flag instanceof Integer) {
                return (int) flag;
            }
            else {
                return 0;
            }
        } catch (ScriptException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.eval", e.getMessage()));
            return 0;
        }
    }

    /**
     * スクリプトファイルの内容を読み込み、再実行する。
     * 関数の二重定義になるとエラーが出る？
     * 関数の定義をするだけであり、実行はイベントごとに行われる。
     * @return パースに成功したらTrue、失敗したらFalse
     */
    private boolean parse() {
        if (this.engine == null || this.scriptFile == null) return false;
        try (FileReader fr = new FileReader(this.scriptFile)) {
            this.engine.eval(fr);
        } catch (FileNotFoundException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.notfound", this.scriptFile.getName()));
            return false;
        } catch (ScriptException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.eval", e.getMessage()));
            return false;
        } catch (IOException e) {
            GTS.LOGGER.error(I18n.format("gts.exception.script_engine.io"));
            return false;
        }
        return true;
    }

    protected Map<String, Object> getBindJavaObjects() {
        Map<String, Object> map = new HashMap<>();
        map.put("binding", new GTSBinding());
        return map;
    }

    public File getScriptFile() {
        return this.scriptFile;
    }

    /**
     * スクリプトファイルをセットし、パースし直す。
     * 成功しようが失敗しようがこのメソッドは終了するので注意。
     * @param scriptFile 新しいスクリプトファイル
     */
    public void setScriptFile(File scriptFile) {
        this.scriptFile = scriptFile;
        this.parse();
    }

    /**
     * なんかJava側の機能呼び出すためのもの
     * いろいろ追加予定。
     */
    public static class GTSBinding {

        public void log(Object obj) {
            GTS.LOGGER.debug("Script Engine said: {}", obj.toString());
        }
    }


}
