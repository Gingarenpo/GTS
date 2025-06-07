package com.gfactory.gts.common;

import com.gfactory.gts.common.controller.GTSCycle;
import com.gfactory.gts.common.controller.GTSPhase;
import com.gfactory.gts.pack.config.GTSConfig;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * サイクルの定義情報、フェーズの定義情報などを動的に読み込むためのクラス。
 * アドオンとして将来的に追加できるように、読み込まれたクラスをもとに探して、GSONの
 * サブクラスとして登録するためのもの。
 */
public class GTSClassScanner {

    /**
     * staticで扱うことを基本とするためインスタンスの作成は禁止する
     */
    private GTSClassScanner() {}

    public static List<Class<? extends GTSCycle>> findCycleClass() {
        List<Class<? extends GTSCycle>> result = new ArrayList<>(); // 結果を保持する変数を作成

        // ClassGraphを使用してクラスをスキャン
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages("com.gfactory.gts").scan()) {
            // サブクラスのリストを取得
            ClassInfoList subclasses = scanResult.getSubclasses(GTSCycle.class.getName());

            // 全部のクラスをまとめて入れる
            for (ClassInfo classInfo: subclasses) {
                // 抽象クラスも入ってくるのでそれは省く
                if (classInfo.isAbstract()) continue;

                // クラスを追加する（未検査キャスト出るけど仕方ない
                result.add((Class<? extends GTSCycle>) classInfo.loadClass());
            }
        }

        return result;
    }

    public static List<Class<? extends GTSPhase>> findPhaseClass() {
        List<Class<? extends GTSPhase>> result = new ArrayList<>(); // 結果を保持する変数を作成

        // ClassGraphを使用してクラスをスキャン
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages("com.gfactory.gts").scan()) {
            // サブクラスのリストを取得
            ClassInfoList subclasses = scanResult.getSubclasses(GTSPhase.class.getName());

            // 全部のクラスをまとめて入れる
            for (ClassInfo classInfo: subclasses) {
                // 抽象クラスも入ってくるのでそれは省く
                if (classInfo.isAbstract()) continue;

                // クラスを追加する（未検査キャスト出るけど仕方ない
                result.add((Class<? extends GTSPhase>) classInfo.loadClass());
            }
        }

        return result;
    }

    public static List<Class<? extends GTSConfig>> findConfigClass() {
        List<Class<? extends GTSConfig>> result = new ArrayList<>(); // 結果を保持する変数を作成

        // ClassGraphを使用してクラスをスキャン
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages("com.gfactory.gts").scan()) {
            // サブクラスのリストを取得
            ClassInfoList subclasses = scanResult.getSubclasses(GTSConfig.class.getName());

            // 全部のクラスをまとめて入れる
            for (ClassInfo classInfo: subclasses) {
                // 抽象クラスも入ってくるのでそれは省く
                if (classInfo.isAbstract()) continue;

                // クラスを追加する（未検査キャスト出るけど仕方ない
                result.add((Class<? extends GTSConfig>) classInfo.loadClass());
            }
        }

        return result;
    }
}
