package com.gfactory.gts.minecraft.gui.widget;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.MathHelper;

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
    private final TreeMap<String, ? extends GTSConfig> choices;

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
        // 要素の大きさの背景を描く
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x20FFFFFF);

        // 現在のスクロール位置からどこまで描画可能かを選択
        int start = this.scrollOffset;
        int end = this.scrollOffset + this.height;

        int absoluteY = 0; // 絶対座標
        for (Map.Entry<String, ? extends GTSConfig> entry: choices.entrySet()) {
            if (absoluteY >= start) {
                // 描画対象の場合
                if (Objects.equals(this.selectedChoice, entry.getKey())) {
                    Gui.drawRect(this.x, this.y + absoluteY - start, this.x + this.width, this.y + absoluteY - start + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, 0x8000FF00);
                }
                Minecraft.getMinecraft().fontRenderer.drawString(entry.getKey(), this.x + 2, this.y + absoluteY - start, 0xFFFFFF);
            }
            absoluteY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }

        // スクロールバーを書く
        if (this.height >= this.maxHeight) return; // スクロールする必要がない
        int barLength = (int) (((float)this.height / this.maxHeight) * this.height); // スクロールバーの大きさ
        int barTop = (int) (((float)this.scrollOffset / this.maxHeight) * this.height); // スクロールバーの位置
        Gui.drawRect(this.x + this.width - BAR_WIDTH, this.y + barTop, this.x + this.width, this.y + barTop + barLength, 0xFF00FF00);
    }


    @Override
    public void handleMouseInput(int mouseX, int mouseY, float mouseWheel, int mouseButton) throws IOException {
        boolean inBounds = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;
        if (!inBounds) return;

        this.scrollOffset -= Math.signum(mouseWheel) * 5 * (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, this.maxHeight - this.height);
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
}
