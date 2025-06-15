package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.helper.GNBTHelper;
import com.gfactory.core.mqo.MQOFace;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.common.sign.GTS114Sign;
import com.gfactory.gts.common.sign.GTSSignBase;
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
    private double width = 1.6f;

    /**
     * 地名板の高さ
     */
    private double height = 0.8f;

    /**
     * 地名板の厚さ
     */
    private double depth = 0.05f;

    /**
     * テクスチャそのもののリソースロケーション（パック内の看板用PNG探るので）
     */
    private ResourceLocation texture;

    /**
     * 地名板生成で使用するものの場合、ここにその情報が入る
     */
    private GTSSignBase info;

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
        this.texture = null;
        this.info = new GTS114Sign();
        this.info.japanese = "ダミー";
        this.info.english = "DUMMY";

    }

    @Override
    public void readDataFromNBT(NBTTagCompound compound) {
        // 座標たち
        this.width = GNBTHelper.getDoubleWithValue(compound, "gts_sign_width", this.width);
        this.height = GNBTHelper.getDoubleWithValue(compound, "gts_sign_height", this.height);
        this.depth = GNBTHelper.getDoubleWithValue(compound, "gts_sign_depth", this.depth);

        // テクスチャがある場合はそれを読み込む
        if (compound.hasKey("gts_sign_texture")) {
            this.texture = new ResourceLocation(compound.getString("gts_sign_texture"));
            this.info = null;
        }
        else {
            // 地名板の情報を読み込む
            this.texture = null;
            if (!compound.hasKey("gts_sign_info_type") || compound.getString("gts_sign_info_type").equals("GTS114Sign")) {
                this.info = new GTS114Sign();
                this.info.readFromNBT(compound.getCompoundTag("gts_sign_info"));
            }
            else {
                // TODO: 他の標示板はelse ifブロックで書く。ここはどうしようもない場合なので普通来ない
                this.info = new GTSSignBase();
                this.info.readFromNBT(compound.getCompoundTag("gts_sign_info"));
            }
        }
    }

    @Override
    public NBTTagCompound writeDataToNBT(NBTTagCompound compound) {
        // 座標たち
        compound.setDouble("gts_sign_width", this.width);
        compound.setDouble("gts_sign_height", this.height);
        compound.setDouble("gts_sign_depth", this.depth);

        // 地名板の種類
        if (this.texture != null && !this.isGenerated()) {
            // テクスチャ直指定の場合はそれを入れる（リソースロケーションを文字列にして）
            compound.setString("gts_sign_texture", texture.toString());
        }
        if (this.isGenerated()) {
            // 地名板指定の場合はその地名板の情報をすべて入れる
            if (this.info instanceof GTS114Sign) {
                compound.setTag("gts_sign_info", ((GTS114Sign) this.info).writeToNBT());
            }
        }

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
        MQOVertex v3 = new MQOVertex(width, height, depth);
        MQOVertex v4 = new MQOVertex(width, -height, depth);
        MQOVertex v5 = new MQOVertex(width, height, -depth);
        MQOVertex v6 = new MQOVertex(width, -height, -depth);
        MQOVertex v7 = new MQOVertex(-width, height, -depth);
        MQOVertex v8 = new MQOVertex(-width, -height, -depth);
        // 6面を作成（表と裏以外適当）
        MQOFace f1 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0, 0},
                        new double[] {0.9, 0.0},
                        new double[] {0.9, 0.45},
                        new double[] {0, 0.45}
                },
                new int[] {0, 2, 3, 1},
                4
        ); // 正面
        MQOFace f2 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0, 0.45},
                        new double[] {0.9, 0.45},
                        new double[] {0.9, 0.9},
                        new double[] {0, 0.9}
                },
                new int[] {2, 4, 5, 3},
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
                new int[] {4, 6, 7, 5},
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
                new int[] {6, 0, 1, 7},
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
                new int[] {6, 4, 2, 0},
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
                new int[] {1, 3, 5, 7},
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
        this.buildObject();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        this.buildObject();
    }

    public void setObject(MQOObject object) {
        this.object = object;
    }

    public ResourceLocation getTexture() {
        if ((this.texture == null || this.texture.equals(GTSSignTextureManager.PLACE_HOLDER)) && this.isGenerated()) {
            this.texture = GTS.SIGN_MANAGER.getResourceLocation(this.info);
        }
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
        this.buildObject();
    }

    public GTSSignBase getInfo() {
        return info;
    }

    public void setInfo(GTSSignBase info) {
        this.info = info;
    }

    /**
     * この地名板は動的に生成されたものであるかどうか
     * @return 動的の場合はtrue
     */
    public boolean isGenerated() {
        return this.info != null;
    }

    @Override
    public String toString() {
        return "GTSTileEntityTrafficSign{" +
                "depth=" + depth +
                ", width=" + width +
                ", height=" + height +
                ", texture=" + texture +
                ", info=" + info +
                ", object=" + object +
                ", angle=" + angle +
                ", config=" + config +
                ", modelMinMax=" + Arrays.toString(modelMinMax) +
                ", pack=" + pack +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                ", blockType=" + blockType +
                ", pos=" + pos +
                ", tileEntityInvalid=" + tileEntityInvalid +
                ", world=" + world +
                "} " + super.toString();
    }
}
