package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficSignConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * 地名板・標示板などを描画するためのTESR
 * モデルは固定で四角形、大きさはTileEntityの方で指定する。
 * VBOを用いた描画に特化しているため、専用のVBOを呼び出すことで描画を効率よく行う。
 */
public class GTSTileEntityTrafficSignRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficSign, GTSTrafficSignConfig> {

    @Override
    public void renderModel(GTSTileEntityTrafficSign te, GTSPack pack, GTSTrafficSignConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // もろもろあるので回転を元に戻す
        GlStateManager.rotate((float) -te.getAngle(), 0, 1, 0);
        // モデルを取得
        MQOObject body = te.getObject();
        // テクスチャを取得
        ResourceLocation texture = te.getTexture();
        if (body == null) return; // 準備ができていない場合は描画しない
        if (texture != null) this.bindTexture(texture); // なぜかテクスチャnullだと落ちることがあるので初期中の初期は適当なテクスチャ

        // TODO: パフォーマンスを計測し、重くなるのであれば止める
        // アングルを考慮して奥にポールがいる場合吸い寄せる

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
        body.draw();
    }
}
