package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.common.controller.GTSCycle;
import com.gfactory.gts.common.controller.GTSPhase;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GTSTileEntityTrafficLightRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficLight, GTSTrafficLightConfig> {

    @Override
    public void renderModel(GTSTileEntityTrafficLight te, GTSPack pack, GTSTrafficLightConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null || pack == null || config == null) return;
        // 各種テクスチャを取得
        ResourceLocation textureBase = te.getPack().getOrCreateBindTexture(config.getTextures().getBase());
        ResourceLocation textureLight = te.getPack().getOrCreateBindTexture((((GTSTrafficLightConfig.GTSTrafficLightTexture) config.getTextures())).getLight());
        ResourceLocation textureNoLight = te.getPack().getOrCreateBindTexture((((GTSTrafficLightConfig.GTSTrafficLightTexture) config.getTextures())).getNoLight());
        if (textureBase == null || textureLight == null || textureNoLight == null) return;

        this.bindTexture(textureBase);
        for (MQOObject o: model.getObjects()) {
            if (config.getBody().contains(o.getName())) o.draw();
        }

        // アタッチされている制御機がある場合は、その制御機から発光部分を取得する
        // 発光部分は陰影とかフル無視
        // アタッチされててもなんもサイクルがない場合は無視
        if (te.getAttachedTrafficController() == null) return;
        TileEntity ttt = te.getWorld().getTileEntity(te.getAttachedTrafficController());
        if (!(ttt instanceof GTSTileEntityTrafficController)) return;
        GTSTileEntityTrafficController tttt = (GTSTileEntityTrafficController) ttt;
        GTSCycle cycle = tttt.getNowCycle();
        if (cycle == null) return;
        GTSPhase phase = cycle.getNowPhase();
        if (phase == null) return;
        GTSTrafficLightConfig.GTSTrafficLightPattern p = tttt.getNowCycle().getNowPhase().getChannels().get(te.getChannel());
        if (p == null) return;
        // Tickが0である、またはTickが0でなく点滅周期の最初の場合は点灯
        boolean light = p.getTick() == 0 || (p.getTick() != 0 && (phase.getTicks() % p.getTick()) < (p.getTick() / 2) );

        // 先に光らない部分を描画しないとならない
        this.bindTexture(textureNoLight);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        if (textureLight.equals(textureNoLight)) {
            // 発光部分が非発光部分と同一の場合、大抵光ってしまうので強制的に暗くする
            // Optifineとか使って明るさかえるとバグるから
            GL11.glColor3f(0.5f, 0.5f, 0.5f); // 強制的に黒色をブレンドして暗くする
        }
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 60f, 60f); // バニラだとこれでも暗くなる
        for (MQOObject o: model.getObjects()) {
            if (p.getObjects().contains(o.getName()) && light) continue;
            if (config.getLight().contains(o.getName())) o.draw();
        }

        // 光る部分を描画する
        this.bindTexture(textureLight);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        for (MQOObject o: model.getObjects()) {
            // 光るオブジェクトに入っていなければ問答無用で描画しない
            if (!config.getLight().contains(o.getName())) continue;
            if (p.getObjects().contains(o.getName()) && light) o.draw();
        }
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
