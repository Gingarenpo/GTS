package jp.gingarenpo.gingacore.mqo;

import java.io.Serializable;

/**
 * 面の描画で使用する頂点情報を格納したクラスです。Vectorと同じだけど独自機能追加していくつもりなので一応…
 *
 * @author 銀河連邦
 */
public class MQOVertex implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;

	private double x;
	private double y;
	private double z; // 以上、座標数値
	

	public MQOVertex(String vnum) {
		// MQOの頂点記述方式に従って格納
		// 0.12345 0.23456 0.34567と3つの数値がスペースで区切られている
		// 正規化してあること前提での処理

		final String[] v = vnum.split(" "); // 分割して…
		//System.out.println("Vertex["+v.length+"]");
		if (v.length != 3) // MQOとして不適切
			throw new MQO.MQOException("Illegal Vertex Position!!");

		// 代入していく
		x = Double.parseDouble(v[0]);
		y = Double.parseDouble(v[1]);
		z = Double.parseDouble(v[2]);

		// 終わり
	}
	
	public MQOVertex clone() {
		try {
			return (MQOVertex) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}


	// 正規化に必要なためsetも入れているが外部からの呼び出しは不可能（同一パッケージ内のみ）

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	void setX(double x) {
		this.x = x;
	}

	void setY(double y) {
		this.y = y;
	}

	void setZ(double z) {
		this.z = z;
	}
	
	
}
