package com.gfactory.gts.minecraft.proxy;

import com.gfactory.gts.common.GTSPackLoader;
import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.GTSGuiHandler;
import com.gfactory.gts.minecraft.network.packet.GTSPacketItemNBT;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.*;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * 各イベントにおいて、クライアントとサーバーを考えるとき、どっちかでしか実行してほしくないようなことがある。
 * その場合に、このProxyの機能を使って機能を分断する。
 * ここはその基本となるところで、サーバー・クライアントで共通して行うことを記載する。
 * ただ、サーバーオンリーの処理は基本的にない。Minecraft起動時にどっちか勝手に選ばれてインスタンスが作られる。
 */
public class GTSProxy {
    public void preInit(FMLPreInitializationEvent event) {


        // テクスチャマネージャーの登録
        GTS.SIGN_MANAGER = GTSSignTextureManager.getInstance();

        // TileEntityの登録
        GameRegistry.registerTileEntity(GTSTileEntityTrafficLight.class, new ResourceLocation(GTS.MODID, "traffic_light"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficController.class, new ResourceLocation(GTS.MODID, "traffic_controller"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficPole.class, new ResourceLocation(GTS.MODID, "traffic_pole"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficSign.class, new ResourceLocation(GTS.MODID, "traffic_sign"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficButton.class, new ResourceLocation(GTS.MODID, "traffic_button"));

        // Capabilityの登録
        GTSCapabilities.register();

        GTS.LOADER = new GTSPackLoader();
        // config フォルダの親ディレクトリ（= ゲームのルートディレクトリ）を取得
        File gameDir = event.getModConfigurationDirectory().getParentFile();
        File gtsDir = new File(gameDir, "mods/GTS");

        // ディレクトリが存在しない場合は自動作成
        if (!gtsDir.exists()) {
            gtsDir.mkdirs();
        }

        GTS.LOADER.searchPacks(gtsDir);

    }

    public void init(FMLInitializationEvent event) {
        // GUIハンドラの登録
        NetworkRegistry.INSTANCE.registerGuiHandler(GTS.instance, new GTSGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
        // ネットワークの登録
        GTS.NETWORK.registerMessage(new GTSPacketTileEntity(),
                GTSPacketTileEntity.class,
                1,
                Side.SERVER);
        GTS.NETWORK.registerMessage(new GTSPacketItemNBT(),
                GTSPacketItemNBT.class,
                2,
                Side.SERVER);
    }


    public void registerResourcePack(List<GTSPack> packs) {
        // カラ実装
    }

    public ResourceLocation getOrCreateBindTexture(String name, BufferedImage image) {
        return null;
    }

}
