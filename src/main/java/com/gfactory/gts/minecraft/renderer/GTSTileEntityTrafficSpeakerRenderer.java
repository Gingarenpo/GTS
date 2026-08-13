package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSpeaker;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficSpeakerConfig;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GTSTileEntityTrafficSpeakerRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficSpeaker, GTSTrafficSpeakerConfig> {
    @Override
    public void renderModel(GTSTileEntityTrafficSpeaker te, GTSPack pack, GTSTrafficSpeakerConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
