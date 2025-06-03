package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.mqo.MQOFace;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.config.GTSTrafficSignConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

/**
 * 地名板・標示板等の設置を行うTileEntity。
 * 地名板に限らず適当な画像を貼り付けることも可能だが、
 * それならRTMとかでやった方がいいと思う。
 * 下位互換みたいなものと思っていただければ。
 */
public class GTSTileEntityTrafficSign extends GTSTileEntity {

    /**
     * 地名板の幅
     */
    private double width = 2.0f;

    /**
     * 地名板の高さ
     */
    private double height = 1.0f;

    /**
     * 地名板の厚さ
     */
    private double depth = 0.05f;

    /**
     * テクスチャそのもののリソースロケーション（パック内の看板用PNG探るので）
     */
    private ResourceLocation texture;

    /**
     * MQOObjectを自前で作成する。直方体なので簡単。
     */
    private transient MQOObject object;

    public GTSTileEntityTrafficSign() {
        this.setDummy();
    }

    @Override
    public String getName() {
        return I18n.format("tile.traffic_sign.name");
    }

    @Override
    public void setDummy() {
        // パックとコンフィグはダミーのものを登録しておく
        // 実際にそこを見ることはない
        this.pack = GTS.LOADER.getDummy();
        this.config = new GTSTrafficSignConfig();
        this.config.setDummy();

    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {

    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        return compound;
    }

    private void buildObject() {
        // 8つの頂点を作成する
        // 左上、左下、右下、右上、右上奥、右下奥、左下奥、左上奥
        double width = this.width / 2f;
        double height = this.height / 2f;
        double depth = this.depth / 2f;
        MQOVertex v1 = new MQOVertex(-width, height, depth);
        MQOVertex v2 = new MQOVertex(-width, -height, depth);
        MQOVertex v3 = new MQOVertex(width, -height, depth);
        MQOVertex v4 = new MQOVertex(width, height, depth);
        MQOVertex v5 = new MQOVertex(width, height, -depth);
        MQOVertex v6 = new MQOVertex(width, -height, -depth);
        MQOVertex v7 = new MQOVertex(-width, -height, -depth);
        MQOVertex v8 = new MQOVertex(-width, height, -depth);
        // 6面を作成（表と裏以外適当）
        MQOFace f1 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0, 0},
                        new double[] {0, 0.45},
                        new double[] {0.9, 0.45},
                        new double[] {0.9, 0}
                },
                new int[] {0, 1, 2, 3},
                4
        ); // 正面
        MQOFace f2 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0, 0.45},
                        new double[] {0, 0.9},
                        new double[] {0.9, 0.9},
                        new double[] {0.9, 0.45}
                },
                new int[] {4, 5, 6, 7},
                4
        ); // 背面
        MQOFace f3 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {0.9, 1},
                        new double[] {1, 1},
                        new double[] {1, 0.9}
                },
                new int[] {3, 2, 5, 4},
                4
        ); // 右側面
        MQOFace f4 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {0.9, 1},
                        new double[] {1, 1},
                        new double[] {1, 0.9}
                },
                new int[] {7, 6, 1, 0},
                4
        ); // 左側面
        MQOFace f5 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {0.9, 1},
                        new double[] {1, 1},
                        new double[] {1, 0.9}
                },
                new int[] {7, 0, 3, 4},
                4
        ); // 上
        MQOFace f6 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {0.9, 1},
                        new double[] {1, 1},
                        new double[] {1, 0.9}
                },
                new int[] {2, 1, 6, 5},
                4
        ); // 右側面

        MQOObject o = new MQOObject("body");
        o.getVertexs().addAll(Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8));
        o.getFaces().addAll(Arrays.asList(f1, f2, f3, f4, f5, f6));
        o.buildVBO();
        this.object = o;
    }

    public MQOObject getObject() {
        if (this.object == null) this.buildObject();
        return this.object;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setObject(MQOObject object) {
        this.object = object;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
