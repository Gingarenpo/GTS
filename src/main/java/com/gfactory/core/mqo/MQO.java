package com.gfactory.core.mqo;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * MQOファイル（Ver4.7）までに対応するように作り直した、MQOファイル内の情報を扱うクラス。
 * OpenGLでの描画を想定しており、頂点情報、面情報、UV情報、材質情報、オブジェクト情報、法線情報を格納する。
 * ただし、法線に関してはMQOファイルには存在しないので計算をして格納する。
 * これ以外の情報は読み込まない（プラグインとか）。
 * 
 * OpenGLのVBOに対応しており、GPUを使用して描画を効率化させることができる。
 * Minecraft用に作成しているものであり、DirectX等には非対応。
 * LWJGLを使用しているため、前提としてこちらが必要となる（極力使用しないようにするが）。
 * 
 */
public final class MQO {
	
	/**
	 * このMQOファイルのサムネイルデータを格納する。
	 * 通常はnullとなっており、Ver4以降で勝手に生成されるものを
	 * 使いたい人だけ使おう、という意味合いでフィールドを用意している。
	 * オリジナルのサムネイルデータは192KB近くあり、こちらは
	 * テクスチャを実際に作成するためかなり時間がかかる。
	 * 始めの1回しか作らない。
	 */
	private BufferedImage thumbnail = null;
	
	/**
	 * MQOフォーマットのバージョン。一応保持しておく。特に関係ない。
	 */
	private float version;

	/**
	 * MQOオブジェクト。
	 */
	private ArrayList<MQOObject> objects = new ArrayList<>();

	MQO() {}

	public ArrayList<MQOObject> getObjects() {
		return objects;
	}

	public void setObjects(ArrayList<MQOObject> objects) {
		this.objects = objects;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	/**
	 * サムネイルのイメージを作成する。ピクセル数が少ない場合はいいが、かなり重い処理なので注意
	 * @param rawData RGB生データ
	 */
	void createThumbnailImage(int[][] rawData) throws IOException {
		BufferedImage image = new BufferedImage(rawData.length, rawData[0].length, BufferedImage.TYPE_3BYTE_BGR);
		for (int w = 0; w < rawData.length; w++) {
			for (int h = 0; h < rawData[0].length; h++) {
				image.setRGB(w, h, rawData[w][h]);
			}
		}
		this.thumbnail = image;
	}

	/**
	 * サムネイル画像を返す。ただし基本null
	 * @return あれば返す
	 */
	public BufferedImage getThumbnail() {
		return this.thumbnail;
	}

	public String toString() {
		return "MQO[O="+ this.objects+"]";
	}

	/**
	 * このMQOモデルを、最大辺が指定したサイズになるように拡大縮小を行う。
	 * 同時に原点正規化を行う。例えば、3と指定した場合は-1.5～1.5の間に
	 * 最大辺が収まるような状態になる。
	 *
	 * このMQOモデル自体に変更は加えず、新しいMQOインスタンスを作成する。
	 * なお、リサイズ処理はかなり重たいので注意。
	 * @param maxSize 長辺の最大値
	 * @return そのサイズにリサイズし、原点をモデルの中心に移動させた新しいMQOインスタンス
	 */
	public MQO normalize(double maxSize) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		// 全頂点からバウンディングボックスを取得
		for (MQOObject obj : this.objects) {
			for (MQOVertex v : obj.getVertexs()) {
				minX = Math.min(minX, v.getX());
				minY = Math.min(minY, v.getY());
				minZ = Math.min(minZ, v.getZ());
				maxX = Math.max(maxX, v.getX());
				maxY = Math.max(maxY, v.getY());
				maxZ = Math.max(maxZ, v.getZ());
			}
		}

		double centerX = (minX + maxX) / 2.0;
		double centerY = (minY + maxY) / 2.0;
		double centerZ = (minZ + maxZ) / 2.0;

		double lenX = maxX - minX;
		double lenY = maxY - minY;
		double lenZ = maxZ - minZ;
		double maxLen = Math.max(lenX, Math.max(lenY, lenZ));

		double scale = maxSize / maxLen;

		MQO result = new MQO();
		result.setVersion(this.version);

		// オブジェクトを複製して正規化
		for (MQOObject obj : this.objects) {
			MQOObject newObj = new MQOObject(obj.getName());

			// 頂点の変換
			ArrayList<MQOVertex> newVertexs = new ArrayList<>();
			for (MQOVertex v : obj.getVertexs()) {
				double x = (v.getX() - centerX) * scale;
				double y = (v.getY() - centerY) * scale;
				double z = (v.getZ() - centerZ) * scale;
				newVertexs.add(new MQOVertex(x, y, z));
			}
			newObj.setVertexs(newVertexs);

			// 面情報はそのままコピー（インデックスは変わらない前提）
			newObj.setFaces(obj.getFaces());

			result.getObjects().add(newObj);
		}

		return result;
	}

}
