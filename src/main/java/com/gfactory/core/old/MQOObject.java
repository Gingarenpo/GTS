package com.gfactory.core.old;

import com.gfactory.gts.minecraft.GTS;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * MQOのオブジェクトごとに、面と頂点を格納したものとなります。（頂点番号が重複するためこうしなくてはならない）
 * @author 銀河連邦
 *
 */
public class MQOObject implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;

	private MQOVBOData vboData;
	
	// モデルの面（フェイス）
	ArrayList<MQOFace> face = new ArrayList<MQOFace>(); // 面を番号ごとに格納
	
	// モデルの頂点
	ArrayList<MQOVertex> vertex = new ArrayList<MQOVertex>(); // 頂点を番号ごとに格納
	
	// モデルオブジェクトの名前
	String name; // 必ずあるはずよ

	
	public MQOObject(String name) {
		this.name = name; // 名前
	}
	
	public ArrayList<MQOFace> getFaces() {
		return face;
	}
	
	private void setFaces(ArrayList<MQOFace> face) {
		this.face = face;
	}
	
	public ArrayList<MQOVertex> getVertexs() {
		return vertex;
	}
	
	private void setVertexs(ArrayList<MQOVertex> vertex) {
		this.vertex = vertex;
	}
	
	public String getName() {
		return name;
	}
	
	
	/**
	 * 指定した色を使用してこのオブジェクトを描画するバッファを返します。
	 *
	 * @param color 色（0=自動シャドー）
	 */
	public void draw(BufferBuilder b, float color) {
		for (MQOFace f : this.face) {
			f.drawFace(b, color);
		}
	}
	
	/**
	 * 全部クローンする
	 * @return
	 */
	public MQOObject clone() {
		MQOObject clone = new MQOObject(this.name);
		for (MQOFace f : this.face) {
			clone.face.add(f.clone());
		}
		for (MQOVertex v : this.vertex) {
			clone.vertex.add(v.clone());
		}
		return clone;
	}

	/**
	 * 現在の頂点・辺・UVマッピングから、VBOオブジェクトを作成します。
	 */
	public void buildVBO() {
		// 頂点データを格納するためのFloatBufferを作成
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(this.vertex.size() * 5);

		// XYZUVXYZUV…
        for (MQOVertex mqoVertex : this.vertex) {
            // 頂点の座標を入力
            vertexData.put((float) mqoVertex.getX()).put((float) mqoVertex.getY()).put((float) mqoVertex.getZ());
            // この頂点番号に対応するUVを取得
            boolean inner = false;
            for (MQOFace f : this.face) {
                for (int j = 0; j < f.getV().length; j++) {
                    if (inner) break;
                    if (mqoVertex.getX() == f.getV()[j].getX()
                            && mqoVertex.getY() == f.getV()[j].getY()
                            && mqoVertex.getZ() == f.getV()[j].getZ()) {
                        double[] uv = f.getUv().get(j);
                        // 格納
                        vertexData.put((float) uv[0]).put((float) uv[1]);
                        inner = true;
                        break;
                    }
                }
            }
        }
		vertexData.flip();

		// 面のIndexを作成（三角面とする）
		int size = 0;
		for (MQOFace f: this.face) {
			size += (f.isTriangle()) ? 1 : 2;
		}
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(size * 3);
		for (MQOFace f: this.face) {
			GTS.LOGGER.info("あえｗけをえをｗｑこ" + Arrays.toString(f.getVnum()));
			indexBuffer.put(f.getVnum()[0]).put(f.getVnum()[1]).put(f.getVnum()[2]);
			if (f.getV().length == 4) {
				// 四角形の場合は2回呼び出す。
				// 頂点番号は1,2,3 / 3,4,1
				indexBuffer.put(f.getVnum()[2]).put(f.getVnum()[3]).put(f.getVnum()[0]);
			}
		}
		indexBuffer.flip();

		// これらをGPUに登録する
		int vbo = GL15.glGenBuffers(); // バッファ作って
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); // バッファ番号を指定して
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
		int ibo = GL15.glGenBuffers(); // バッファ作って
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, ibo); // バッファ番号を指定して
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

		// データ作成
		this.vboData = new MQOVBOData(indexBuffer, ibo, vbo, vertexData);

	}

	public MQOVBOData getVboData() {
		return vboData;
	}

	public void drawWithVBO() {
		if (this.vboData == null) this.buildVBO();

		// バッファを割り当て
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboData.getVid());
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY); // XYZUVで割り当て
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 5 * 4, 0);
		GL11.glVertexPointer(2, GL11.GL_FLOAT, 5 * 4, 3 * 4); // どこからどこまでか指定
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vboData.getV().limit());
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // バッファを取り消し
	}
}
