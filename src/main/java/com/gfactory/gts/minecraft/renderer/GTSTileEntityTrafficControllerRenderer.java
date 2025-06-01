package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficControllerConfig;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GTSTileEntityTrafficControllerRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficController, GTSTrafficControllerConfig> {
    @Override
    public void renderModel(GTSTileEntityTrafficController te, GTSPack pack, GTSTrafficControllerConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // テクスチャの指定を行う
        ResourceLocation textureBase = pack.getOrCreateBindTexture(config.getTextures().getBase());

        if (textureBase == null) {
            // テクスチャが見つからないという緊急事態だが落とすのはまずいのでエラー出しまくる
            GTS.LOGGER.warn(I18n.format("gts.warning.texture_cannot_load", "Some Texture"));
            return; // 描画をしない
        }

        // 描画は全オブジェクト
        this.bindTexture(textureBase);
        for (MQOObject o: model.getObjects()) {
            o.draw();
        }
    }
}
