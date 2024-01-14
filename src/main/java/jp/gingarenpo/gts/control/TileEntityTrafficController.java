package jp.gingarenpo.gts.control;

import jp.gingarenpo.gts.base.GTSTileEntity;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 交通信号制御機に関するTileEntity。制御機のあるところにその制御機の
 * データを保管しておく。特にそれ以外追加するものは存在しない。
 */
public class TileEntityTrafficController extends GTSTileEntity<DataTrafficController, ConfigTrafficController> {

	/**
	 * タグをもとにしてデータのインスタンスを返却する。
	 *
	 * @param compound クラスの情報が詰まっていると思われるタグ。
	 * @return インスタンス。
	 */
	@Override
	public DataTrafficController createData(NBTTagCompound compound) {
		DataTrafficController d = new DataTrafficController();
		d.readFromNBT(compound);
		return d;
	}
}
