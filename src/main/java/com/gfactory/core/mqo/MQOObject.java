package com.gfactory.core.mqo;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * MQOのオブジェクトを扱うためのクラス。
 * オブジェクトは、頂点、面、材質の情報を持つ。
 * このオブジェクトが最小描画単位となる。
 * なお、法線情報はMQOに存在しないため、自力で計算する必要がある。
 */
public final class MQOObject {
    /**
     * このオブジェクトが持つ面情報。
     */
    private ArrayList<MQOFace> faces = new ArrayList<>();

    /**
     * このオブジェクトが持つ頂点情報。
     */
    private ArrayList<MQOVertex> vertexs = new ArrayList<>();

    /**
     * このオブジェクトの法線情報。
     */
    private final ArrayList<MQOVertex> normals = new ArrayList<>();

    /**
     * スムージング角度（デフォルトはメタセコでよくあるやつ
     */
    private double smoothing = Math.toRadians(59.5);

    /**
     * このオブジェクトの名前。
     */
    private String name;

    /**
     * このオブジェクトのVBOデータ。
     */
    private MQOVBOData vboData;

    /**
     * このオブジェクトの最小値・最大値
     */
    private double[][] minMax;

    /**
     * 指定した名前のMQOObjectを格納する。
     * @param name オブジェクト名
     */
    public MQOObject(String name) {
        this.name = name;
    }

    public ArrayList<MQOFace> getFaces() {
        return faces;
    }

    public void setFaces(ArrayList<MQOFace> faces) {
        this.faces = faces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<MQOVertex> getVertexs() {
        return vertexs;
    }

    public void setVertexs(ArrayList<MQOVertex> vertexs) {
        this.vertexs = vertexs;
    }

    public String toString() {
        return "MQOObject("+this.name+")[V="+this.vertexs.size()+", F="+this.faces.size()+"]";
    }

    public String toDetailString() {
        return "MQOObject("+this.name+")[V="+this.vertexs+", F="+this.faces+"]";
    }

    /**
     * VBOデータを作成する。現在の頂点、面データをもとに作成する。
     * VBOを使用する場合、頂点と面の整合性がきちんととれていることを確認してから実行すること。
     * このメソッド内では行わない。
     * なおfor3重ループとか超絶非効率なことをやっているのは仕方ないとして。VBO完全に理解しているからこそ（笑）
     *
     * ※法線作ろうとしたらなんかおかしいので、一応こっちは残しておくけど基本は
     * 色で強制的に。。。
     */
    private void buildVBO(boolean useNormal) {

        // 三角形に分割された面
        ArrayList<MQOFace> triangles = new ArrayList<>();
        for (MQOFace face : this.faces) {
            triangles.addAll(Arrays.asList(face.splitTriangleFace()));
        }

        // 法線が未計算なら先に計算
        List<MQOVertex> normals = this.calcFaceVertexNormals(triangles);

        // 頂点数 = 面数 * 3
        FloatBuffer vs = BufferUtils.createFloatBuffer(triangles.size() * 3 * 8); // XYZ + UV + NNN
        IntBuffer indices = BufferUtils.createIntBuffer(triangles.size() * 3);

        int vertexIndex = 0;

        for (MQOFace face : triangles) {
            int[] vIds = face.getVertexId();
            double[][] uvs = face.getUv();

            for (int i = 0; i < 3; i++) {
                MQOVertex v = this.vertexs.get(vIds[i]);
                MQOVertex n = normals.get(vertexIndex);
                double[] uv = uvs[i];

                vs.put((float) v.getX());
                vs.put((float) v.getY());
                vs.put((float) v.getZ());

                vs.put((float) uv[0]);
                vs.put((float) uv[1]);

                if (useNormal) {
                    vs.put((float) n.getX());
                    vs.put((float) n.getY());
                    vs.put((float) n.getZ());
                }
                else {
                    // shadowColorを計算する
                    double shadowColor = this.getShadowColor(n);
                    vs.put((float) shadowColor);
                    vs.put((float) shadowColor);
                    vs.put((float) shadowColor); // 3つとも同じ
                }

                indices.put(vertexIndex++);
            }
        }

        vs.flip();
        indices.flip();

        this.vboData = new MQOVBOData(indices, vs);
        this.vboData.setUseNormal(useNormal);
        this.vboData.loadGPU();
    }

    /**
     * 法線が不安定なのでしばらくは固定値で呼び出す
     */
    public void buildVBO() {
        this.buildVBO(true);
    }

    /**
     * 太陽面を一番上として、そこから180度到達するまでに浴びる光を計算し、0.3～1.0までに整形する
     * @param normal 法線
     * @return 数値
     */
    private double getShadowColor(MQOVertex normal) {
        Vec3d verticalVec = new Vec3d(0, -1, 0); // Y軸に反垂直（＝太陽光）のベクトル
        Vec3d faceVec = new Vec3d(normal.getX(), normal.getY(), normal.getZ()).normalize();

        double dot = faceVec.dotProduct(verticalVec); // 内積を求める(-1.0～1.0）
        dot = (dot + 1) / 2.0d; // 0～1.0にする
        return dot;

    }




    /**
     * このオブジェクトを実際に描画する。
     * 予めOpenGLが使用できる状況であること、原点および回転などの行列操作は終えておくこと。
     * あくまで現在のコンテキストに対してオブジェクトをVBOを用いて描画する。
     * なお、VBOがない場合は作成を試みる。
     * Minecraftの仕様に忠実なのでLWJGL使う。
     */
    public void draw() {
        if (this.vboData == null) {
            this.buildVBO();
            return;
        }
        if (!this.vboData.isGPULoaded()) {
            this.vboData.loadGPU();
            return;
        }

        // 現在のバッファを保存
        int prevVBO = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

        // 法線の自動ノーマライズ有効化（これは固定機能なのでoptional）
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        // VBOバインド
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboData.getVid());

        // 頂点属性の有効化
        GL20.glEnableVertexAttribArray(0); // 位置
        GL20.glEnableVertexAttribArray(1); // UV
        if (this.vboData.isUseNormal()) {
            GL20.glEnableVertexAttribArray(2); // 法線
        } else {
            GL20.glEnableVertexAttribArray(3); // 色
        }

        // 頂点属性ポインタの設定
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0L);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 8 * Float.BYTES, 3 * Float.BYTES);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        if (this.vboData.isUseNormal()) {
            GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        } else {
            GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        }

        // 描画
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vboData.getVertexVBO().capacity() / 8);

        // 頂点属性の無効化
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        if (this.vboData.isUseNormal()) {
            GL20.glDisableVertexAttribArray(2);
        } else {
            GL20.glDisableVertexAttribArray(3);
        }
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        // VBOバインド解除
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevVBO);
    }


    /**
     * 現在の頂点、面の情報を利用して、法線情報を計算する。
     * 全操作なので少し重い。
     */
    private void calcNormal() {
        // 三角形の面を対象に法線を計算するため、分割する
        ArrayList<MQOFace> triangleFaces = new ArrayList<>();
        for (MQOFace face : this.faces) {
            triangleFaces.addAll(Arrays.asList(face.splitTriangleFace()));
        }

        // 面法線の算出用リストと、頂点と面の対応マッピングを作る
        ArrayList<MQOVertex> faceNormals = new ArrayList<>();
        Map<Integer, List<Integer>> vertexToFaceMap = new HashMap<>(); // 頂点をもとにどの面を共有しているかのマッピング
        for (int i = 0; i < this.vertexs.size(); i++) {
            vertexToFaceMap.put(i, new ArrayList<>());
        }

        for (int i = 0; i < triangleFaces.size(); i++) {
            MQOFace f = triangleFaces.get(i);
            // この面を構成する全頂点を取得する
            MQOVertex v0 = this.vertexs.get(f.getVertexId()[0]);
            MQOVertex v1 = this.vertexs.get(f.getVertexId()[1]);
            MQOVertex v2 = this.vertexs.get(f.getVertexId()[2]);

            // 面法線は0, 1, 2の3つの頂点を基本として、01ベクトル・02ベクトルを外積することで得られる
            // ベクトルを求めて
            double[] b1 = new double[] {v1.getX() - v0.getX(), v1.getY() - v0.getY(), v1.getZ() - v0.getZ()};
            double[] b2 = new double[] {v2.getX() - v0.getX(), v2.getY() - v0.getY(), v2.getZ() - v0.getZ()};

            // 外積
            double[] cross = new double[] {
                    b1[1] * b2[2] - b1[2] * b2[1],
                    b1[2] * b2[0] - b1[0] * b2[2],
                    b1[0] * b2[1] - b1[1] * b2[0]
            };

            // 正規化
            double length = Math.sqrt(Math.pow(cross[0], 2) + Math.pow(cross[1], 2) + Math.pow(cross[2], 2));
            MQOVertex faceNormal = new MQOVertex(cross[0] / length, cross[1] / length, cross[2] / length);
            faceNormals.add(faceNormal);

            // 頂点が持つ面リストに追加
            for (int vid: f.getVertexId()) {
                vertexToFaceMap.get(vid).add(i);
            }
        }

        // スムージング角度をcosに変換し、比較用に整形
        double cos = Math.cos(this.smoothing);

        // 頂点法線をスムージング角度により変換
        for (int i = 0; i < this.vertexs.size(); i++) {
            List<Integer> relatedFaces = vertexToFaceMap.get(i); // この頂点を持つ面をすべて取得
            MQOVertex normalSum = new MQOVertex(0, 0, 0); // 合成用ベクトル

            // 基準法線として自分の属する各面を順番に処理（平均する面を決定する）
            for (int fi : relatedFaces) {
                MQOVertex baseNormal = faceNormals.get(fi);

                // 基準面との角度がスムージング角未満の面の法線を加算
                for (int fj : relatedFaces) {
                    MQOVertex compNormal = faceNormals.get(fj);

                    // 内積を計算（単位ベクトルなのでcosθに相当）
                    double dot = baseNormal.getX() * compNormal.getX()
                            + baseNormal.getY() * compNormal.getY()
                            + baseNormal.getZ() * compNormal.getZ();

                    if (dot >= cos) {
                        normalSum.setX(normalSum.getX() + compNormal.getX());
                        normalSum.setY(normalSum.getY() + compNormal.getY());
                        normalSum.setZ(normalSum.getZ() + compNormal.getZ());
                    }
                }
                // 1つの基準面につき1回でいい
                break;
            }

            // 正規化して normals に追加
            double len = Math.sqrt(
                    normalSum.getX() * normalSum.getX() +
                            normalSum.getY() * normalSum.getY() +
                            normalSum.getZ() * normalSum.getZ()
            );

            if (len > 0) {
                this.normals.add(new MQOVertex(
                        normalSum.getX() / len,
                        normalSum.getY() / len,
                        normalSum.getZ() / len
                ));
            } else {
                // 法線がゼロベクトルの場合、Y軸方向のデフォルトを与える
                this.normals.add(new MQOVertex(0, 1, 0));
            }
        }

    }

    public List<MQOVertex> calcFaceVertexNormals(List<MQOFace> triangleFaces) {
        ArrayList<MQOVertex> faceNormals = new ArrayList<>();
        Map<Integer, List<Integer>> vertexToFaceMap = new HashMap<>();
        for (int i = 0; i < this.vertexs.size(); i++) {
            vertexToFaceMap.put(i, new ArrayList<>());
        }

        for (int i = 0; i < triangleFaces.size(); i++) {
            MQOFace f = triangleFaces.get(i);
            int[] vId = f.getVertexId();
            MQOVertex v0 = this.vertexs.get(vId[0]);
            MQOVertex v1 = this.vertexs.get(vId[1]);
            MQOVertex v2 = this.vertexs.get(vId[2]);

            double[] b1 = new double[] {v1.getX() - v0.getX(), v1.getY() - v0.getY(), v1.getZ() - v0.getZ()};
            double[] b2 = new double[] {v2.getX() - v0.getX(), v2.getY() - v0.getY(), v2.getZ() - v0.getZ()};

            double[] cross = new double[] {
                    b1[1] * b2[2] - b1[2] * b2[1],
                    b1[2] * b2[0] - b1[0] * b2[2],
                    b1[0] * b2[1] - b1[1] * b2[0]
            };

            double length = Math.sqrt(cross[0]*cross[0] + cross[1]*cross[1] + cross[2]*cross[2]);
            MQOVertex fn = new MQOVertex(cross[0]/length, cross[1]/length, cross[2]/length);
            faceNormals.add(fn);

            for (int vid : vId) {
                vertexToFaceMap.get(vid).add(i);
            }
        }

        double cos = Math.cos(this.smoothing);
        List<MQOVertex> vertexNormals = new ArrayList<>();

        for (int i = 0; i < triangleFaces.size(); i++) {
            MQOFace face = triangleFaces.get(i);
            for (int j = 0; j < 3; j++) {
                int vertexId = face.getVertexId()[j];
                MQOVertex normalSum = new MQOVertex(0, 0, 0);
                MQOVertex currentNormal = faceNormals.get(i);

                for (int fi : vertexToFaceMap.get(vertexId)) {
                    MQOVertex otherNormal = faceNormals.get(fi);
                    double dot = currentNormal.getX() * otherNormal.getX() + currentNormal.getY() * otherNormal.getY() + currentNormal.getZ() * otherNormal.getZ();
                    if (dot >= cos) {
                        normalSum.setX(normalSum.getX() + otherNormal.getX());
                        normalSum.setY(normalSum.getY() + otherNormal.getY());
                        normalSum.setZ(normalSum.getZ() + otherNormal.getZ());
                    }
                }

                double len = Math.sqrt(normalSum.getX()*normalSum.getX() + normalSum.getY()*normalSum.getY() + normalSum.getZ()*normalSum.getZ());
                if (len > 0) {
                    vertexNormals.add(new MQOVertex(normalSum.getX()/len, normalSum.getY()/len, normalSum.getZ()/len));
                } else {
                    vertexNormals.add(new MQOVertex(0, 1, 0));
                }
            }
        }

        return vertexNormals; // 各三角形×3 の頂点ごとの法線（順序通り）
    }



    /**
     * このオブジェクトの法線情報を返す。ない場合は計算する。
     * @return 法線情報
     */
    public ArrayList<MQOVertex> getNormals() {
        if (this.normals.isEmpty()) this.calcNormal();
        return this.normals;
    }

    public double getSmoothing() {
        return smoothing;
    }

    public void setSmoothing(double smoothing) {
        this.smoothing = smoothing;
    }


    /**
     * このオブジェクトのXYZそれぞれに対し、最小値と最大値を返す。
     * 戻り値は1次元が3、2次元が2の配列となる。
     * @return XYZ（この順番）におけるMinMax（この順番）
     */
    public double[][] getAxisMinMax() {
        if (this.minMax == null) return calcAxisMinMax();
        return this.minMax;
    }

    /**
     * このモデルのバウンディングボックスを返す。
     * 戻り値はMQOVertexとなり、0番目に最小値、1番目に最大値が格納されている
     * @return バウンディングボックスのXYZ最小値～XYZ最大値
     */
    public MQOVertex[] getBoundingBox() {
        double[][] minMax = this.getAxisMinMax();
        return new MQOVertex[] {new MQOVertex(minMax[0][0], minMax[1][0], minMax[2][0]), new MQOVertex(minMax[0][1], minMax[1][1], minMax[2][1])};
    }

    /**
     * 計算が重いためキャッシュするために別だし
     * @return 各軸における最小値と最大値
     */
    private double[][] calcAxisMinMax() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (MQOVertex v: this.vertexs) {
            minX = Math.min(minX, v.getX());
            maxX = Math.max(maxX, v.getX());
            minY = Math.min(minY, v.getY());
            maxY = Math.max(maxY, v.getY());
            minZ = Math.min(minZ, v.getZ());
            maxZ = Math.max(maxZ, v.getZ());
        }
        this.minMax = new double[][] { new double[] {minX, maxX},  new double[] {minY, maxY},  new double[] {minZ, maxZ}};
        return new double[][] { new double[] {minX, maxX},  new double[] {minY, maxY},  new double[] {minZ, maxZ}};
    }
}
