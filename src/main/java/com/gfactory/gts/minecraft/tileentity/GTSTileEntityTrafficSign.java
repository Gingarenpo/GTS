package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.core.helper.GNBTHelper;
import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOFace;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.core.mqo.MQOVertex;
import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.common.sign.GTS114Sign;
import com.gfactory.gts.common.sign.GTSSignBase;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficSignConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
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
            // テクスチャを取得
            this.info = null;
            this.texture = null;
            String textureData = compound.getString("gts_sign_texture");
            System.out.println("Load: " + textureData);
            if (textureData.isEmpty()) return;
            String[] metaData = textureData.split("@");
            if (metaData.length != 2) return; // 無視
            // 0=パック名、1=テクスチャ名
            if (!this.pack.getName().equals(metaData[0])) {
                // パックが違う場合は更新し、コンフィグも強制更新する
                GTSPack pack = GTS.LOADER.getPack(metaData[0]);
                if (pack == null) return; // パックがおかしい場合は更新しない
                this.pack = pack;
            }
            // コンフィグの更新
            GTSTrafficSignConfig config = (GTSTrafficSignConfig) this.config;
            config.setTexture(metaData[1]);

            this.config = config;
            // サーバーの場合テクスチャ取得の必要はないので無視
            if (this.world == null || !this.world.isRemote) return;

            // テクスチャ取得
            ResourceLocation rs = this.pack.getOrCreateBindTexture(metaData[1]);
            this.texture = rs;
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
        // isGenerated() の判定に頼らず、pack と config の存在をチェックして保存する
        if (this.info == null && this.pack != null && this.config != null) {
            GTSTrafficSignConfig signConfig = (GTSTrafficSignConfig) this.config;
            if (signConfig.getTextures() != null && signConfig.getTextures().getBase() != null) {
                System.out.println("save: " + this.pack.getName() + "@" + signConfig.getTextures().getBase());
                compound.setString("gts_sign_texture", this.pack.getName() + "@" + signConfig.getTextures().getBase());
            }
        }
        else if (this.isGenerated()) {
            // 地名板指定の場合はその地名板の情報をすべて入れる
            if (this.info instanceof GTS114Sign) {
                compound.setString("gts_sign_info_type", "GTS114Sign");
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

        // 6面を作成
        MQOFace f1 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.0, 0.0},
                        new double[] {0.9, 0.0},
                        new double[] {0.9, 0.45},
                        new double[] {0.0, 0.45}
                },
                new int[] {0, 2, 3, 1},
                4
        ); // 正面 [「表面」領域: U 0.0~0.9, V 0.0~0.45]

        MQOFace f2 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {1.0, 0.9},
                        new double[] {1.0, 1.0},
                        new double[] {0.9, 1.0}
                },
                new int[] {2, 4, 5, 3},
                4
        ); // 右側面

        MQOFace f3 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.0, 0.45},
                        new double[] {0.9, 0.45},
                        new double[] {0.9, 0.9},
                        new double[] {0.0, 0.9}
                },
                new int[] {4, 6, 7, 5},
                4
        ); // 背面 [「裏面」領域: U 0.0~0.9, V 0.45~0.90]

        MQOFace f4 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {1.0, 0.9},
                        new double[] {1.0, 1.0},
                        new double[] {0.9, 1.0}
                },
                new int[] {6, 0, 1, 7},
                4
        ); // 左側面

        MQOFace f5 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {1.0, 0.9},
                        new double[] {1.0, 1.0},
                        new double[] {0.9, 1.0}
                },
                new int[] {6, 4, 2, 0},
                4
        ); // 上面

        MQOFace f6 = new MQOFace(
                0,
                new double[][] {
                        new double[] {0.9, 0.9},
                        new double[] {1.0, 0.9},
                        new double[] {1.0, 1.0},
                        new double[] {0.9, 1.0}
                },
                new int[] {1, 3, 5, 7},
                4
        ); // 下面

        MQOObject o = new MQOObject("body");
        o.getVertexs().addAll(Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8));
        o.getFaces().addAll(Arrays.asList(f1, f2, f3, f4, f5, f6));
        o.buildVBO();
        this.object = o;
    }

    public MQO getObject() {
        if (this.object == null) this.buildObject();
        MQO res = new MQO();
        ArrayList<MQOObject> obj = new ArrayList();
        obj.add(this.object);

        res.setObjects(obj);
        return res;
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
        // 動的生成画像の場合
        if (this.isGenerated()) {
            if (this.texture == null || this.texture.equals(GTSSignTextureManager.PLACE_HOLDER)) {
                this.texture = GTS.SIGN_MANAGER.getResourceLocation(this.info);
            }
        }
        // パックの既存テクスチャの場合（描写スレッドで安全に割り当てる）
        else {
            if (this.texture == null && this.pack != null && this.config != null) {
                GTSTrafficSignConfig signConfig = (GTSTrafficSignConfig) this.config;
                if (signConfig.getTextures() != null && signConfig.getTextures().getBase() != null) {
                    this.texture = this.pack.getOrCreateBindTexture(signConfig.getTextures().getBase());
                }
            }
        }
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
        this.info = null;
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
        this.texture = null;
    }

    /**
     * この地名板は動的に生成されたものであるかどうか。
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
