package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.minecraft.GTS;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * GTS二ブロックを登録するためのイベントフック
 */
@Mod.EventBusSubscriber
public class GTSBlocks {

    /**
     * 交通信号機
     */
    public static final GTSBlockTrafficLight TRAFFIC_LIGHT = new GTSBlockTrafficLight();
    public static final Item ITEM_TRAFFIC_LIGHT = new ItemBlock(TRAFFIC_LIGHT).setRegistryName(TRAFFIC_LIGHT.getRegistryName());

    /**
     * 交通信号制御機
     */
    public static final GTSBlockTrafficContoller TRAFFIC_CONTROLLER = new GTSBlockTrafficContoller();
    public static final Item ITEM_TRAFFIC_CONTROLLER = new ItemBlock(TRAFFIC_CONTROLLER).setRegistryName(TRAFFIC_CONTROLLER.getRegistryName());

    /**
     * 交通信号柱
     */
    public static final GTSBlockTrafficPole TRAFFIC_POLE = new GTSBlockTrafficPole();
    public static final Item ITEM_TRAFFIC_POLE = new ItemBlock(TRAFFIC_POLE).setRegistryName(TRAFFIC_POLE.getRegistryName());

    /**
     * 地名板・標示板等
     */
    public static final GTSBlockTrafficSign TRAFFIC_SIGN = new GTSBlockTrafficSign();
    public static final Item ITEM_TRAFFIC_SIGN = new ItemBlock(TRAFFIC_SIGN).setRegistryName(TRAFFIC_SIGN.getRegistryName());

    /**
     * ブロックをレジストリに追加する。これを行わないとMinecraft内でブロックを扱えない。
     * これで登録することでブロックIDが割り当てられる。
     * @param event イベント
     */
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(TRAFFIC_LIGHT);
        event.getRegistry().register(TRAFFIC_CONTROLLER);
        event.getRegistry().register(TRAFFIC_POLE);
        event.getRegistry().register(TRAFFIC_SIGN);
    }

    /**
     * アイテムをレジストリに追加する。ブロックもItemBlockとして追加しないと、
     * インベントリに登録されず設置できなくなる。
     * @param event イベント
     */
    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ITEM_TRAFFIC_LIGHT);
        event.getRegistry().register(ITEM_TRAFFIC_CONTROLLER);
        event.getRegistry().register(ITEM_TRAFFIC_POLE);
        event.getRegistry().register(ITEM_TRAFFIC_SIGN);

    }

    /**
     * アイテムのモデルを登録する。GUIではここで登録しないとミッシングテクスチャになってしまう。
     * @param event イベント
     */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // モデルロケーションを登録
        ModelLoader.setCustomModelResourceLocation(
                GTSBlocks.ITEM_TRAFFIC_LIGHT,
                0,
                new ModelResourceLocation(new ResourceLocation(GTS.MODID, "traffic_light"), "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GTSBlocks.ITEM_TRAFFIC_CONTROLLER,
                0,
                new ModelResourceLocation(new ResourceLocation(GTS.MODID, "traffic_controller"), "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GTSBlocks.ITEM_TRAFFIC_POLE,
                0,
                new ModelResourceLocation(new ResourceLocation(GTS.MODID, "traffic_pole"), "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GTSBlocks.ITEM_TRAFFIC_SIGN,
                0,
                new ModelResourceLocation(new ResourceLocation(GTS.MODID, "traffic_sign"), "inventory")
        );
    }
}
