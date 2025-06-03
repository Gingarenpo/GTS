package com.gfactory.gts.minecraft.proxy;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.block.GTSBlocks;
import com.gfactory.gts.minecraft.renderer.GTSTileEntityTrafficControllerRenderer;
import com.gfactory.gts.minecraft.renderer.GTSTileEntityTrafficLightRenderer;
import com.gfactory.gts.minecraft.renderer.GTSTileEntityTrafficPoleRenderer;
import com.gfactory.gts.minecraft.renderer.GTSTileEntityTrafficSignRenderer;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * クライアント側のみで行う処理。
 * モデルの登録やTileEntitySpecialRendererとかの処理は基本的にこっち
 */
public class GTSClientProxy extends GTSProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        // GTSのクリエイティブタブを登録
        GTS.TAB = new CreativeTabs("gts_tab") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(Item.getItemFromBlock(GTSBlocks.TRAFFIC_LIGHT));
            }
        };
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // TileEntitySpecialRendererの登録
        ClientRegistry.bindTileEntitySpecialRenderer(GTSTileEntityTrafficLight.class, new GTSTileEntityTrafficLightRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(GTSTileEntityTrafficController.class, new GTSTileEntityTrafficControllerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(GTSTileEntityTrafficPole.class, new GTSTileEntityTrafficPoleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(GTSTileEntityTrafficSign.class, new GTSTileEntityTrafficSignRenderer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
}
