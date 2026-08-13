package com.gfactory.gts.minecraft.proxy;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.block.GTSBlocks;
import com.gfactory.gts.minecraft.renderer.*;
import com.gfactory.gts.minecraft.tileentity.*;
import com.gfactory.gts.pack.GTSMemoryResourcePack;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.awt.image.BufferedImage;
import java.util.List;

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
        ClientRegistry.bindTileEntitySpecialRenderer(GTSTileEntityTrafficButton.class, new GTSTileEntityTrafficButtonRenderer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    /**
     * GTS Loaderの読み込み結果によって、IResourcePackを再読み込みさせるもの。
     * マルチプレイのサポートに伴って、これをクライアント側で保持すると色々厄介なことになるため
     * リソースパックの適用をプロキシに退避させることにした。
     * @param packs
     */
    @Override
    public void registerResourcePack(List<GTSPack> packs) {
        super.registerResourcePack(packs);
        for (GTSPack pack: packs) {
            addDefaultResourcePack(new GTSMemoryResourcePack(pack));
        }

        Minecraft.getMinecraft().refreshResources(); // 再読み込みをさせて全部のパックを一気に読み込み
    }

    private void addDefaultResourcePack(IResourcePack pack) {
        List<IResourcePack> l = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
        l.add(pack);
    }

    /**
     * 指定されたテクスチャをバインドし、その中身を返す
     * @param name
     * @param image
     * @return
     */
    @Override
    public ResourceLocation getOrCreateBindTexture(String name, BufferedImage image) {
        if (image == null) return null;
        return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, new DynamicTexture(image));
    }
}
