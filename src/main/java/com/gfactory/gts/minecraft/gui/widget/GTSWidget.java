package com.gfactory.gts.minecraft.gui.widget;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;

import java.io.IOException;

/**
 * GTS内部で使用するGUI自作ウィジェット
 */
public abstract class GTSWidget<T extends GTSTileEntity> {

    /**
     * ウィジェットの幅
     */
    public int width;

    /**
     * ウィジェットの高さ
     */
    public int height;

    /**
     * 座標X
     */
    public int x;

    /**
     * 座標Y
     */
    public int y;

    /**
     * TileEntity
     */
    protected T te;

    public GTSWidget(T te, int width, int height, int x, int y) {
        this.height = height;
        this.te = te;
        this.width = width;
        this.x = x;
        this.y = y;
    }

    /**
     * このウィジェットを描画する。
     */
    public abstract void draw();

    @Override
    public String toString() {
        return "GTSWidget{" +
                "height=" + height +
                ", width=" + width +
                ", x=" + x +
                ", y=" + y +
                ", te=" + te +
                '}';
    }

    /**
     * マウスの入力がコンポーネント上であった場合に呼ばれる
     * @throws IOException
     */
    public abstract void handleMouseInput(int mouseX, int mouseY, float mouseWheel, int mouseButton) throws IOException;

    /**
     * マウスがクリックされたときに呼ばれる
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);
}
