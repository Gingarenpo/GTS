package jp.gingarenpo.gts.control;

import jp.gingarenpo.gts.base.NBTConvertible;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 交通信号制御機の情報を保持しておくデータ用クラス。
 * サイクル、フェーズも同時に保管する。サイクルとフェーズはクラスとなっているので
 * シリアライズして保管するようにする。
 *
 */
public class DataTrafficController implements NBTConvertible<DataTrafficController> {

	/**
	 * 交通信号制御機のデータを作成する。初期値のままとする。
	 */
	public DataTrafficController() {
		// NO CODE
	}

	/**
	 * NBTタグを読み取り、その内容からデータを復元する。
	 * @param compound タグ。
	 * @return 自分自身。
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		// フィールドを上から順番に保持する
	}

	/**
	 * 自分自身の情報をNBTタグに書き込み、そのタグを返す。
	 * @param compound 追記すべき必要があるタグ。
	 * @return 追記したタグ。
	 */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return compound;
	}
}
