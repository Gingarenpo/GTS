package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficSignConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * 地名板・標示板などを描画するためのTESR
 * モデルは固定で四角形、大きさはTileEntityの方で指定する。
 * VBOを用いた描画に特化しているため、専用のVBOを呼び出すことで描画を効率よく行う。
 */
public class GTSTileEntityTrafficSignRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficSign, GTSTrafficSignConfig> {

    @Override
    public void renderModel(GTSTileEntityTrafficSign te, GTSPack pack, GTSTrafficSignConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // モデルを取得
        MQOObject body = te.getObject();

        // テクスチャを取得
        ResourceLocation texture = te.getTexture();

        if (body == null || texture == null) return;

        this.bindTexture(texture);
        GlStateManager.translate(0, 0, -0.5+te.getDepth());
        body.draw();
    }
}
