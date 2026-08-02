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
        if (model == null && this.hasModel()) {
            // モデルが存在しない場合描画を中止
            GTS.LOGGER.warn(I18n.format("gts.warning.model_cannot_load", config.getModel()));
            GL11.glPopMatrix();
            return; // 描画をしない
        }
        else if (model == null && !this.hasModel()) {
            // getObjectから直接呼び出す
            model = te.getObject();
        }

        // OpenGL設定
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // ずらす（
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5); // ブロック原点へ移動
        GL11.glRotated(te.getAngle(), 0, 1, 0); // Y軸回転（ラジアン→度に変換）
        GL11.glRotated(te.getRotateX(), 1, 0, 0);
        GL11.glRotated(te.getRotateY(), 0, 1, 0);
        GL11.glRotated(te.getRotateZ(), 0, 0, 1);
        GL11.glTranslated(te.getPosX(), te.getPosY(), te.getPosZ()); // ローカル座標オフセット

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

    /**
     * このレンダーはモデルを持つのかどうか。自前でモデルを描画するときなど、モデルパックに頼らない場合はここでfalseにしておくとモデルを不要に読み込まない。
     * その場合、getObjectから直接取得できる必要がある
     * @return
     */
    public boolean hasModel() {
        return true;
    }
}
