package com.gfactory.gts.minecraft.client;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.proxy.GTSClientProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * クライアント側のイベント処理用機構。
 * こちらでは基本的にサウンド関連のイベント扱いなどを記載する。
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = GTS.MODID, value = Side.CLIENT)
public class GTSClientEventHandler {

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        GTSClientProxy.CLIENT_SOUND_MANAGER.clear();
    }
}
