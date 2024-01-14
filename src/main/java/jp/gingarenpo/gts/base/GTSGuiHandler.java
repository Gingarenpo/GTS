package jp.gingarenpo.gts.base;

import jp.gingarenpo.gts.GTSGuiId;
import jp.gingarenpo.gts.control.GuiContainerTrafficController;
import jp.gingarenpo.gts.control.GuiTrafficController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * GTS内で管理するGUIの取捨選択を行うためのハンドラクラス。
 * 正直まだ説明をきちんとできるレベルまで理解が追い付いていないので
 * 詳しい説明は省くが、サーバーとクライアントでGUIを開くために使われる。
 */
public class GTSGuiHandler implements IGuiHandler {

	/**
	 * サーバー側で与えられた引数をもとにして開くべきGUIを返す。
	 * 開くべきGUIが存在しない場合はNULLを返す。
	 * なお、サーバーの場合はスクリーンを開くと落ちるのでコンテナのみを開く必要がある。
	 *
	 * @param ID GUIのID。
	 * @param player GUIを開こうとしているプレイヤー。
	 * @param world 今いる世界。
	 * @param x X座標。
	 * @param y Y座標。
	 * @param z Z座標。
	 * @return 開けるGUI、なければnull。
	 */
	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		GTSGuiId id = GTSGuiId.value(ID);
		if (id == null) return null;
		switch (id) {
			case TRAFFIC_CONTROLLER:
				return new GuiContainerTrafficController();

			default:
				return null;
		}
	}

	/**
	 * クライアント側で与えられた引数をもとにして開くべきGUIを返す。
	 * 開くべきGUIが存在しない場合はNULLを返す。
	 *
	 * @param ID GUIのID。
	 * @param player GUIを開こうとしているプレイヤー。
	 * @param world 今いる世界。
	 * @param x X座標。
	 * @param y Y座標。
	 * @param z Z座標。
	 * @return 開けるGUI、なければnull。
	 */
	@Nullable
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		GTSGuiId id = GTSGuiId.value(ID);
		if (id == null) return null;
		switch (id) {
			case TRAFFIC_CONTROLLER:
				return new GuiTrafficController();

			default:
				return null;
		}
	}
}
