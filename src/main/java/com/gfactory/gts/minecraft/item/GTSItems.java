package com.gfactory.gts.minecraft.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * アイテムなどを登録するためのレジストリ
 */
@Mod.EventBusSubscriber
public class GTSItems {
    public static final Item ATTACHMENT = new GTSItemAttachment();
    public static final Item TRAFFIC_ARM = new GTSItemTrafficArm();

    /**
     * 実際にゲームに登録する
     * @param event
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ATTACHMENT);
        event.getRegistry().register(TRAFFIC_ARM);
    }

    /**
     * アイテムのモデルを登録する。GUIではここで登録しないとミッシングテクスチャになってしまう。
     * @param event イベント
     */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // モデルロケーションを登録
        ModelLoader.setCustomModelResourceLocation(GTSItems.ATTACHMENT, 0, new ModelResourceLocation(new ResourceLocation("gts", "attachment"), "inventory"));
        ModelLoader.setCustomModelResourceLocation(GTSItems.TRAFFIC_ARM, 0, new ModelResourceLocation(new ResourceLocation("gts", "traffic_arm"), "inventory"));
    }
}
