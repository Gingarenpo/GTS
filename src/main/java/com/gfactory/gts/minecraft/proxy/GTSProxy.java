package com.gfactory.gts.minecraft.proxy;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketItemNBT;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

/**
 * 各イベントにおいて、クライアントとサーバーを考えるとき、どっちかでしか実行してほしくないようなことがある。
 * その場合に、このProxyの機能を使って機能を分断する。
 * ここはその基本となるところで、サーバー・クライアントで共通して行うことを記載する。
 * ただ、サーバーオンリーの処理は基本的にない。Minecraft起動時にどっちか勝手に選ばれてインスタンスが作られる。
 */
public class GTSProxy {
    public void preInit(FMLPreInitializationEvent event) {

        // TileEntityの登録
        GameRegistry.registerTileEntity(GTSTileEntityTrafficLight.class, new ResourceLocation(GTS.MODID, "traffic_light"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficController.class, new ResourceLocation(GTS.MODID, "traffic_controller"));
        GameRegistry.registerTileEntity(GTSTileEntityTrafficPole.class, new ResourceLocation(GTS.MODID, "traffic_pole"));

        // Capabilityの登録
        GTSCapabilities.register();

    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {
        GTS.LOADER.searchPacks(new File(Minecraft.getMinecraft().mcDataDir.getAbsolutePath() + "\\mods\\GTS"));
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

}
