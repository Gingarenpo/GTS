package jp.gingarenpo.gts.control;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * GUIコンテナ。サーバー側でGUIを開いたときに呼び出されるコンテナクラス。
 * スロットなどを使用する場合、ここで処理を行う必要があるが、特にスロットを消費する
 * アイテムは存在しないのでここの処理は基本的にない。
 */
public class GuiContainerTrafficController extends Container {

	/**
	 * このGUIを開くことができるかどうかを返す。
	 *
	 * @param playerIn 開ける場合はtrue、開けない場合はfalse。
	 * @return
	 */
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
}
