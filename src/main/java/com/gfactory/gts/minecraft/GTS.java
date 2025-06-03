package com.gfactory.gts.minecraft;

import com.gfactory.gts.common.GTSClassScanner;
import com.gfactory.gts.common.GTSPackLoader;
import com.gfactory.gts.common.controller.GTSCycle;
import com.gfactory.gts.common.controller.GTSPhase;
import com.gfactory.gts.minecraft.proxy.GTSProxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.RuntimeTypeAdapterFactory;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod エントリーポイント
 */
@Mod(modid = GTS.MODID, name = GTS.NAME, version = GTS.VERSION)
public class GTS {
    /**
     * MODID
     */
    public static final String MODID = "gts";

    /**
     * Modの名前
     */
    public static final String NAME = "GTS - Ginren Traffic System";

    /**
     * Modのバージョン
     */
    public static final String VERSION = "2.0-alpha2";

    /**
     * GTS Logger
     */
    public static final Logger LOGGER = LogManager.getLogger("GTS");

    /**
     * GTSの拡張パックを読み込むローダーインスタンス
     */
    public static final GTSPackLoader LOADER = new GTSPackLoader();

    /**
     * クライアント側で読み込まなくてはならないもの、サーバー側で読み込まなくてはならないものの2種類がある
     * それをどっち側化によって分別してくれるProxyを使用する。メインはサーバー
     */
    @SidedProxy(clientSide = "com.gfactory.gts.minecraft.proxy.GTSClientProxy", serverSide = "com.gfactory.gts.minecraft.proxy.GTSProxy")
    public static GTSProxy proxy;

    /**
     * クリエイティブタブ（GTSのブロックはここに集約する）
     */
    public static CreativeTabs TAB;

    public static Gson GSON;

    /**
     * サーバーとクライアントの通信で使用する為のネットワークラッパー
     */
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(GTS.MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws InterruptedException {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // GTSCycleクラスをスキャンして登録する
        RuntimeTypeAdapterFactory<GTSCycle> typeFactory = RuntimeTypeAdapterFactory.of(GTSCycle.class, "type");
        for (Class<? extends GTSCycle> clazz: GTSClassScanner.findCycleClass()) {
            GTS.LOGGER.debug(I18n.format("gts.message.find.class", clazz.getName()));
            typeFactory.registerSubtype(clazz);
        }

        // GTSPhaseクラスをスキャンして登録する
        RuntimeTypeAdapterFactory<GTSPhase> typeFactory2 = RuntimeTypeAdapterFactory.of(GTSPhase.class, "type");
        for (Class<? extends GTSPhase> clazz: GTSClassScanner.findPhaseClass()) {
            GTS.LOGGER.debug(I18n.format("gts.message.find.class", clazz.getName()));
            typeFactory2.registerSubtype(clazz);
        }
        // ここでやっとGSONを読み込めるがまあゲーム開始時以降しか使わないはずだから平気。
        // GTSLoaderではここは使わない予定。使う場合はInitで出してしまう。
        GSON = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .registerTypeAdapterFactory(typeFactory2)
                .setPrettyPrinting()
                .create();

        proxy.postInit(event);
    }

}
