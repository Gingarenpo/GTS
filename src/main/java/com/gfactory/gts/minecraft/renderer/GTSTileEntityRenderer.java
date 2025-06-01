package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

/**
 * 共通のレンダー部分に関してはこちらで処理することにする。
 * 初期処理のみを行う。
 * @param <T> GTSTileEntityを継承したもの
 * @param <U> GTSConfigを継承したもの
 */
public abstract class GTSTileEntityRenderer<T extends GTSTileEntity, U extends GTSConfig> extends TileEntitySpecialRenderer<GTSTileEntity> {

    /**
     * 実際にこのモデルを描画する。
     * @param te 描画すべきTileEntity
     * @param x 描画X座標（なぜ実数になっているのかは不明）
     * @param y 描画Y座標
     * @param z 描画Z座標
     * @param partialTicks 0.0～1.0の範囲で、最後のTickからの経過ミリTickを返す
     * @param destroyStage 破壊されている場合、そのステージを掲載する。今回関係ない
     * @param alpha 透明度
     */
    @Override
    public void render(GTSTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        // 現在のOpenGLの設定を保存する
        GL11.glPushMatrix();

        // コンフィグデータとパックデータを取得する
        U config = (U) te.getConfig();
        GTSPack pack = te.getPack();
        if (pack == null) {
            GL11.glPopMatrix();
        }
        MQO model = pack.getResizingModels(config.getModel(), config.getSize());
        if (model == null) {
            // モデルが存在しない場合描画を中止
            GTS.LOGGER.warn(I18n.format("gts.warning.model_cannot_load", config.getModel()));
            GL11.glPopMatrix();
            return; // 描画をしない
        }

        // OpenGL設定
        GlStateManager.enableRescaleNormal();
        GL11.glEnable(GL11.GL_NORMALIZE);
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // ずらす
        GL11.glTranslated(
                x + 0.5 + te.getPosX() * Math.cos(te.getAngle()) + te.getPosZ() * Math.sin(te.getAngle()),
                y + 0.5 + te.getPosY(),
                z + 0.5 + te.getPosZ() * Math.cos(te.getAngle()) + te.getPosX() * Math.sin(te.getAngle())
        ); // ブロックの原点を描画対象の座標に移動させる（ただしMQOの性質上原点を中心に移動させる）
        GL11.glRotated(te.getAngle(), 0f, 1f, 0f); // 回転させる

        // 描画
        this.renderModel((T) te, pack, config, Tessellator.getInstance(), model, x, y, z, partialTicks, destroyStage, alpha);

        // 元に戻す
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

    /**
     * <h1>描画用メソッド</h1>
     * <p>TileEntityからパック、モデル、コンフィグが渡され、OpenGLのコンテキストが整った（初期化された）状態で渡される。</p>
     * <p>原状回復などを気にせずに書いてよい、単純なモデル描画用メソッド。追加の設定はしても構わない。</p>
     * @param te TileEntityそのもの。
     * @param pack パック。
     * @param config コンフィグ。
     * @param t テッセレーター。描画に使う。
     * @param model モデル。
     * @param x 座標X
     * @param y 座標Y
     * @param z 座標Z
     * @param partialTicks 最後のTickからの経過ミリTick
     * @param destroyStage 破壊レベル（使いません）
     * @param alpha 透明度（使いません）
     */
    public abstract void renderModel(
            T te, GTSPack pack, U config, Tessellator t, MQO model,
            double x, double y, double z, float partialTicks, int destroyStage, float alpha);
}
