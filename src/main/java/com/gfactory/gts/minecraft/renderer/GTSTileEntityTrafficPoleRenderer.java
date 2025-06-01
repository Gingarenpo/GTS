package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficPoleConfig;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class GTSTileEntityTrafficPoleRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficPole, GTSTrafficPoleConfig>{
    @Override
    public void renderModel(GTSTileEntityTrafficPole te, GTSPack pack, GTSTrafficPoleConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // テクスチャの指定を行う
        ResourceLocation textureBase = pack.getOrCreateBindTexture(config.getTextures().getBase());

        if (textureBase == null) {
            // テクスチャが見つからないという緊急事態だが落とすのはまずいのでエラー出しまくる
            GTS.LOGGER.warn(I18n.format("gts.warning.texture_cannot_load", "Some Texture"));
            return; // 描画をしない
        }

        this.bindTexture(textureBase);

        // 接続部分を取得
        ArrayList<String> obj = config.getNormalObject();
        if (te.isUpJoint() && !te.isBottomJoint()) {
            // 上だけ
            ArrayList<String> obj2 = config.getUpJointObject();
            if (obj2 != null) obj = obj2;
        }
        else if (!te.isUpJoint() && te.isBottomJoint()) {
            // 下だけ
            ArrayList<String> obj2 = config.getBottomJointObject();
            if (obj2 != null) obj = obj2;
        }
        else if (te.isUpJoint() && te.isBottomJoint()) {
            // 下だけ
            ArrayList<String> obj2 = config.getFullJointObject();
            if (obj2 != null) obj = obj2;
        }

        for (MQOObject o: model.getObjects()) {
            if (obj.contains(o.getName())) o.draw();
        }
    }
}
