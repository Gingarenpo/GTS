package com.gfactory.gts.minecraft.gui.widget;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * GUI内部でスクロールを可能としたペイン。
 * 内部には
 */
public class GTSGuiScrollList extends GTSWidget<GTSTileEntity> {

    /**
     * この要素がスクロール要素として持つもの
     */
    private TreeMap<String, ? extends GTSConfig> choices;

    /**
     * 選択中の要素
     */
    private String selectedChoice = "";

    /**
     * スクロールできる最大位置
     */
    private int maxHeight;

    public static final int BAR_WIDTH = 5;

    /**
     * どれくらいスクロールしたか（基準：0）
     */
    private int scrollOffset;

    public GTSGuiScrollList(GTSTileEntity te, int width, int height, int x, int y, TreeMap<String, ? extends GTSConfig> choices) {
        super(te, width, height, x, y);
        this.choices = choices;

        this.maxHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * choices.size();
    }

    /**
     * 現在選択されているコンフィグを返す
     * @return ない場合はnull
     */
    public GTSConfig getSelectedChoice() {
        return this.choices.get(selectedChoice);
    }

    @Override
    public void draw() {
        Minecraft mc = Minecraft.getMinecraft();

        // 1. 背景を描画
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x20FFFFFF);

        // 2. Scissor Test による描画範囲（クリッピング領域）の計算と有効化
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();

        // OpenGLの座標系は「左下」が原点のためY座標の反転変換が必要
        int scissorX = this.x * scale;
        int scissorY = mc.displayHeight - (this.y + this.height) * scale;
        int scissorWidth = this.width * scale;
        int scissorHeight = this.height * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

        // 3. リスト要素の描画
        int fontHeight = mc.fontRenderer.FONT_HEIGHT;
        int start = this.scrollOffset;
        int end = this.scrollOffset + this.height;

        int absoluteY = 0; // 絶対座標
        for (Map.Entry<String, ? extends GTSConfig> entry : choices.entrySet()) {
            // 表示範囲に少しでも重なる要素のみ処理
            if (absoluteY + fontHeight >= start && absoluteY <= end) {
                int drawY = this.y + absoluteY - start;

                if (Objects.equals(this.selectedChoice, entry.getKey())) {
                    Gui.drawRect(this.x, drawY, this.x + this.width, drawY + fontHeight, 0x8000FF00);
                }
                mc.fontRenderer.drawString(entry.getKey(), this.x + 2, drawY, 0xFFFFFF);
            }
            absoluteY += fontHeight;
        }

        // 4. Scissor Test を無効化（他の描画への影響を防ぐため）
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // 5. スクロールバーの描画（Scissor領域外に出さない場合はここ、またはScissor解除前に行う）
        if (this.height < this.maxHeight) {
            int barLength = (int) (((float) this.height / this.maxHeight) * this.height);
            int barTop = (int) (((float) this.scrollOffset / this.maxHeight) * this.height);
            Gui.drawRect(this.x + this.width - BAR_WIDTH, this.y + barTop, this.x + this.width, this.y + barTop + barLength, 0xFF00FF00);
        }
    }


    @Override
    public void handleMouseInput(int mouseX, int mouseY, float mouseWheel, int mouseButton) throws IOException {
        boolean inBounds = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;
        if (!inBounds) return;

        this.scrollOffset -= Math.signum(mouseWheel) * 5 * (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, Math.max(0, this.maxHeight - this.height));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean inBounds = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;
        if (!inBounds) return;

        // 該当位置にあるべき物を取得
        int start = this.scrollOffset;

        int absoluteY = 0; // 絶対座標
        for (Map.Entry<String, ? extends GTSConfig> entry: choices.entrySet()) {
            if (absoluteY < start) {
                absoluteY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
                continue;
            }
            if (this.y + absoluteY - start <= mouseY && this.y + absoluteY - start + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT >= mouseY) {
                // これを選択状態にする
                if (entry.getValue() == null) continue; // 選択肢が選べない場合は無効
                this.selectedChoice = entry.getKey();
                break;
            }
            absoluteY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }
    }

    public TreeMap<String, ? extends GTSConfig> getChoices() {
        return choices;
    }

    public void setChoices(TreeMap<String, ? extends GTSConfig> choices) {
        this.choices = choices;
        this.maxHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * choices.size();
    }

    /**
     * 何も選択されていない状態に戻す
     */
    public void resetChoice() {
        this.selectedChoice = "";
    }
}
