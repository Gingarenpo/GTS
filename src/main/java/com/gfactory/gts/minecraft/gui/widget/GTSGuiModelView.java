package com.gfactory.gts.minecraft.gui.widget;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * GTSのGUIで使用する、MQOモデルを実際にレンダリングする際に使用するもの。
 * OpenGLを直接利用して描画を行う。
 */
public class GTSGuiModelView<T extends GTSTileEntity> extends GTSWidget {

    public GTSGuiModelView(GTSTileEntity te, int width, int height, int x, int y) {
        super(te, width, height, x, y);
    }

    @Override
    public void draw() {
        // コンフィグモデルを取得
        MQO mqo = this.te.getPack().getResizingModels(this.te.getConfig().getModel(), this.te.getConfig().getSize());
        if (mqo == null) return; // モデルが存在しない場合はエラーを防ぐため描画なし
        // テクスチャを取得
        ResourceLocation textureBase = this.te.getPack().getOrCreateBindTexture(this.te.getConfig().getTextures().getBase());
        if (textureBase == null) return; // テクスチャが存在しない場合はエラーを防ぐため描画なし

        // バウンディングラインを描画（したかったけどできないので背景をやや薄眼の白で埋めることで対応
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x20FFFFFF);

        // モデルのとりあえずBodyを描画する
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.translate(this.x + this.width / 2.0F, this.y + this.height / 2.0F, 100.0F);
        GlStateManager.scale( this.width / this.te.getConfig().getSize() * 0.8, -this.width / this.te.getConfig().getSize() * 0.8, this.width / this.te.getConfig().getSize() * 0.8); // Z軸奥向き
        GlStateManager.rotate(30.0F, -1, 1, 0); // ちょこっと立体感出すために30度横向き
        GlStateManager.rotate(Minecraft.getSystemTime() % 3600 * 0.1f, 0, 1, 0); // Y軸方向にくるくる回す
        RenderHelper.enableStandardItemLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // テクスチャ設定
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureBase);
        for (MQOObject o: mqo.getObjects()) {
            // 信号機の場合、ボディとして適切なものだけを記す
            if (this.te instanceof GTSTileEntityTrafficLight) {
                if (((GTSTileEntityTrafficLight) this.te).getConfig().getBody().contains(o.getName()) || ((GTSTileEntityTrafficLight) this.te).getConfig().getLight().contains(o.getName()) ) o.draw();
            }
            else {
                o.draw();
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();


    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY, float mouseWheel, int mouseButton) throws IOException {
        // NOOP
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // NOOP
    }


}
