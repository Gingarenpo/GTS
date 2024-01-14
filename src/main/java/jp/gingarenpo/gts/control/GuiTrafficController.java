package jp.gingarenpo.gts.control;

import net.minecraft.client.gui.GuiScreen;

/**
 * 交通信号制御機に関する設定を行うことができるGUIを構築する。
 * ハードコーディングになる部分が非常に多くなる部分でもあるが、そもそもレスポンシブに
 * 対応させるのが非常にめんどくさいのでどうしようもないことである。
 */
public class GuiTrafficController extends GuiScreen {

	/**
	 * 1フレームごとに呼び出されるGUI描画メソッド。
	 * ここで、GUIパーツを描画する。
	 *
	 * @param mouseX マウスのX座標。
	 * @param mouseY マウスのY座標。
	 * @param partialTicks 最期のTicksが経過してからの次のTickに移動するまでの間の端数。
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * GUIが最初に構築される際に呼び出されるイニシャライザ。
	 * ここで、ボタン（と言いつつ実はGUIパーツ）の追加を行う。
	 */
	@Override
	public void initGui() {
		super.initGui();
	}

	/**
	 * GUIの背景を描くメソッド。
	 * @param tint ？
	 */
	@Override
	public void drawBackground(int tint) {
		super.drawBackground(tint);
	}

	/**
	 * このGUIが開いている間、ゲームを一時停止するかどうか。
	 * @return 停止するならtrue。
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}
