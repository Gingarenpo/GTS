package jp.gingarenpo.gts.base;

import net.minecraft.nbt.NBTTagCompound;

/**
 * ある機械がアイデンティティを保ってデータを扱えるように、またNBTタグを用いて
 * データを共有することができるように細工を整えたものとなっている。
 * このインターフェースをimplementsしている場合、NBTタグを用いて相互に
 * デシリアライズとシリアライズができる必要がある。
 *
 * なお、このクラスで使用するデータは本来プリミティブ型となっていることが望ましく、
 * そうでない場合はそのクラスがシリアライズ可能となっていることが望ましい。
 *
 * Tは、自分自身のクラスを挿入する。そうすることで、readFromNBTにおいてそのインスタンスが
 * 直接使用可能となる。
 */
public interface NBTConvertible<T extends NBTConvertible<T>> {

	/**
	 * 引数に渡されたNBTタグを解析し、復元したインスタンスを返す。
	 * シリアライズ時のデータを復元する際に使用する。
	 *
	 * @param compound タグ。
	 */
	public void readFromNBT(NBTTagCompound compound);


	/**
	 * 引数に渡されたNBTタグに対して、現在の自分自身のインスタンスを
	 * シリアライズして保存し、そのタグを返す。
	 * パケットでそのまま送受信を行うために必要なメソッド。
	 *
	 * @param compound 追記すべき必要があるタグ。
	 * @return 現在のインスタンスの内容を追記した結果のタグ。
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound compound);

}
