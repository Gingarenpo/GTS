package jp.gingarenpo.gts.control;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gingacore.mqo.MQOObject;
import jp.gingarenpo.gts.GTS;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RendererTrafficController extends TileEntitySpecialRenderer<TileEntityTrafficController> {

	/**
	 * この制御機をレンダリングする。毎フレーム呼び出される。この中に重い処理を書くとその分ゲーム自体も
	 * もっさりする。OpenGLのコンテキスト設定は基本的に初期化されるが、念のためスタックしておくと安心。
	 *
	 * @param te 描画を行う対象のTileEntity。
	 * @param x 描画する座標。なぜか浮動小数点。
	 * @param y 描画する座標。
	 * @param z 描画する座標。
	 * @param partialTicks 1Tickが経過してからの端数Ticksを表す。
	 * @param destroyStage 破壊レベル。日々の表現をしたいときとかに使うもの。
	 * @param alpha 透明度。関係ない？
	 */
	@Override
	public void render(TileEntityTrafficController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		// 現在のOpenGL設定をスタックする
		GL11.glPushMatrix();

		// TileEntityの外観をつかさどるデータをもらう
		ConfigTrafficController config = te.getConfig();

		// テクスチャをセットする
		ResourceLocation r = config.getPack().getOrCreateBindTexture("a.png");
		if (r == null) {
			GTS.LOGGER.warn("Texture Missing -> " + te);
			GL11.glPopMatrix();
			return; // 描画しない
		}
		this.bindTexture(r);

		// モデルを読み込む
		MQO model = config.getPack().getModels().get(config.getModelPath()); // TODO: エラーチェックすべき

		// モデルの描画を行う
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glTranslated(x + 0.5f, y, z + 0.5f); // ブロックの原点を描画対象の座標に移動させる（ただしMQOの性質上原点を中心に移動させる）
		GlStateManager.shadeModel(GL11.GL_SMOOTH); // スムージング

		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
		for (MQOObject o: model.getObjects4Loop()) {
			o.draw(t.getBuffer(), 0);
		}
		t.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT); // 戻します


		// 描画終了、弄った設定をすべて元に戻す
		GL11.glPopMatrix();
	}
}
