package jp.gingarenpo.gts.control;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.base.ConfigBase;
import jp.gingarenpo.gts.pack.Pack;

import java.io.IOException;

import static jp.gingarenpo.gts.GTS.LOADER;

/**
 * 交通信号制御機の外観に関する情報が格納されたデータ群。
 * モデルパックを元に構築される。このコンフィグのインスタンス自体はLoaderにより
 * 生成される。不必要にインスタンスを作りすぎないようにする。
 * パケットで保持するのは、このConfigをある意味シリアライズしたIDの羅列である。
 */
public class ConfigTrafficController extends ConfigBase {
	@Override
	public String toNBTId() {
		// パック名_ID名とする
		return this.pack.getName() + "_" + this.id + "_" + this.modelPath;
	}

	@Override
	public void fromNBTId(String id) {
		// パック名とIDに分解する
		String[] split = id.split("_");
		if (split.length != 3) {
			throw new IllegalArgumentException("Cannot create ConfigTrafficController because BAD ID: " + id);
		}

		// パック名をもとにパックを取得（Loaderから）
		Pack pack = GTS.LOADER.getPack(split[0]);
		if (pack == null) {
			// コンフィグの使用ができないため、ダミーのコンフィグを使用する
			pack = LOADER.getPack(GTS.DUMMY_PACK_NAME); // ※NULLにはならない
		}
		this.pack = pack;
		this.id = split[1]; // 一応下のIDは残しておく（どうせ見ない）
		this.modelPath = split[2]; // モデル名はそのまま入れる

	}
}
