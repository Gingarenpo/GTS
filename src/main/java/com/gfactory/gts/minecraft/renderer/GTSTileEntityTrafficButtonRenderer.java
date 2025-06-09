package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficButtonConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * 押ボタン箱のレンダラー。
 * 検知しているかどうかでテクスチャを変える。
 * また、ポールと一緒に設置された場合のみ、そのポールに合わせて吸着する（ただし、XZ軸のみ）
 */
public class GTSTileEntityTrafficButtonRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficButton, GTSTrafficButtonConfig<GTSTrafficButtonConfig.GTSTrafficButtonTexture>> {
    @Override
    public void renderModel(GTSTileEntityTrafficButton te, GTSPack pack, GTSTrafficButtonConfig<GTSTrafficButtonConfig.GTSTrafficButtonTexture> config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (pack == null || config == null) return;
        ResourceLocation texture = pack.getOrCreateBindTexture((te.isDetected()) ? config.getTextures().getDetected() : config.getTextures().getBase());
        if (texture == null) return;

        GlStateManager.rotate((float) -te.getAngle(), 0, 1, 0);
        TileEntity north = te.getWorld().getTileEntity(te.getPos().north());
        TileEntity east = te.getWorld().getTileEntity(te.getPos().east());
        TileEntity west = te.getWorld().getTileEntity(te.getPos().west());
        TileEntity south = te.getWorld().getTileEntity(te.getPos().south());
        TileEntity up = te.getWorld().getTileEntity(te.getPos().up());
        TileEntity down = te.getWorld().getTileEntity(te.getPos().down());
        MQOVertex point = new MQOVertex(0, 0, 0);
        if (south instanceof GTSTileEntityTrafficPole) {
            // 南（Z+）なのでZminにZを近づける
            double[][] r = ((GTSTileEntityTrafficPole) south).getModelMinMax();
            point.setZ(point.getZ() + 1 + r[2][0]);
        }
        else if (north instanceof GTSTileEntityTrafficPole) {
            // 北（Z-）なのでZmaxにZを近づける
            double[][] r = ((GTSTileEntityTrafficPole) north).getModelMinMax();
            point.setZ(point.getZ() - 1 + r[2][1]);
        }
        else if (east instanceof GTSTileEntityTrafficPole) {
            // 東（X+）なのでXminにXを近づける
            double[][] r = ((GTSTileEntityTrafficPole) east).getModelMinMax();
            point.setX(point.getX() + 1 + r[0][0]);
        }
        else if (west instanceof GTSTileEntityTrafficPole) {
            // 西（X-）なのでXmaxにXを近づける
            double[][] r = ((GTSTileEntityTrafficPole) west).getModelMinMax();
            point.setX(point.getX() - 1 + r[0][1]);
        }
        else if (up instanceof GTSTileEntityTrafficPole) {
            // 上（Y+）なのでYminにYを近づける
            double[][] r = ((GTSTileEntityTrafficPole) up).getModelMinMax();
            point.setY(point.getY() + 1 + r[1][0]);
        }
        else if (down instanceof GTSTileEntityTrafficPole) {
            // 下（Y-）なのでYmaxにYを近づける
            double[][] r = ((GTSTileEntityTrafficPole) down).getModelMinMax();
            point.setY(point.getY() - 1 + r[1][1]);
        }
        GlStateManager.translate(point.getX(), point.getY(), point.getZ());
        GlStateManager.rotate((float) te.getAngle(), 0, 1, 0);

        // 押ボタンは普通に描画
        this.bindTexture(texture);
        for (MQOObject o: model.getObjects()) {
            o.draw();
        }
    }
}
