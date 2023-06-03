package jp.gingarenpo.gts.pack;

import com.google.gson.Gson;
import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.GTS;
import net.minecraftforge.fml.common.ProgressManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Mods内に存在するパックを検索し、そのパック一覧を保持するクラス。
 * GTS1.0ではここに全実態を格納していたが、メモリの管理が大変なことになるため
 * ここではPack自体のリストを保持することとし、実態にはPackからアクセスすることにする。
 *
 * ZIPアクセスを行う場合、それを開きっぱなしにするだけでメモリがだいぶ使われるので、
 * それをおさえるためにZIPアクセスを最小限にする。
 */
public class Loader {

	/**
	 * このローダーインスタンスにて読み込んだ全てのパック。
	 */
	private ArrayList<Pack> packs = new ArrayList<>();


	/**
	 * ローダーインスタンスを初期化する。
	 */
	public Loader() {
		// NO CODE
	}

	/**
	 * ローダーがパス内の検索を開始する。検索されるパスを引数で指定することができる。
	 * そのパス直下にあるZipファイルが検索対象となり、有効なパックはローダーインスタンスに登録され、
	 * どこからでもアクセスすることが可能。なお、fileにnullを指定した場合は何処も検索せずに終了する。
	 *
	 * @param file 検索を行うパス。
	 */
	public void search(File file) {
		// パス内のZIPファイルを見つけ上げ、それの数だけ繰り返す
		if (file == null) return; // nullの場合は検索対象が存在しない

		File[] files = file.listFiles((dir, name) -> name.endsWith(".zip")); // 配下のZIPファイルを探す
		ProgressManager.ProgressBar bar = ProgressManager.push("Addon Search", files.length); // プログレスバーを取得する

		GTS.LOGGER.info("Pack search started.");
		for (File f: files) {
			bar.step(f.getName());
			try (FileInputStream fis = new FileInputStream(f)) {
				try (ZipInputStream zis = new ZipInputStream(fis)) {
					Pack pack = this.load(zis, f);
					if (pack.empty()) throw new IOException(); // 不正なパックの場合は読み込まない
					this.packs.add(pack);
				}
			} catch (IOException e) {
				GTS.LOGGER.error("Cannot open " + f.getName() + ". Is it a valid GTS addon?");
			}
		}
		ProgressManager.pop(bar); // さようならプログレスバー
		GTS.LOGGER.info("Pack search finished.");
	}

	/**
	 * 指定された名前に合致するパックを返す。同じ名称のパックが存在する場合、最初に見つかったパックを
	 * 返すが、何をもって最初とみなすかは不定になっているため、同じ名称のパックが存在する場合に
	 * 全てを取得したい場合はfileを使用する。
	 *
	 * @param name パックの名前。
	 * @return 合致するパックが読み込まれていればそのパック、なければnull。
	 */
	public Pack getPack(String name) {
		for (Pack pack: this.packs) {
			if (pack.getName().equals(name)) return pack;
		}
		return null;
	}

	/**
	 * 指定されたファイルロケーションに合致するパックを返す。ロケーションが同じパックは重複しないため、
	 * ユニークなパックを1つだけ指定する場合はこちらを使用する。
	 *
	 * @param file パックのファイルロケーション。
	 * @return 合致するパックが読み込まれていればそのパック、なければnull。
	 */
	public Pack getPack(File file) {
		for (Pack pack: this.packs) {
			if (pack.getFile().equals(file)) return pack;
		}
		return null;
	}

	/**
	 * ローダーが読み込んだ有効なパックを全て格納したListを返す。
	 * ループなどで回す際に使用する。読み取り専用となるため、
	 * addやremoveは行えない。
	 *
	 * @return 読み込まれているパック。
	 */
	public List<Pack> getPacks() {
		return Collections.unmodifiableList(this.packs);
	}

	/**
	 * 指定されたロケーションに存在するパックの読み込みを行い、その結果を返す。
	 * 指定されたファイルが不正なパックだった場合でも、Packのインスタンスを必ず返すため、
	 * 取得した後にpack.empty()を使用して不正なパックかどうかを判断する必要がある。
	 *
	 * @param zis ZipのInputStream。
	 * @param file ファイルロケーション。
	 * @return 読み込んだ結果のPackインスタンス。ただし、不正な場合も存在する。
	 */
	private Pack load(ZipInputStream zis, File file) throws IOException {
		// ZIPファイルの読み取りを開始する
		Pack pack = new Pack(file.getName(), file);

		// コンフィグデータを読み取るのに必要
		ArrayList<String> configs = new ArrayList<>();

		ZipEntry entry = null; // 一旦格納されるために必要
		while ((entry = zis.getNextEntry()) != null) {
			this.loadModel(pack, entry, zis);
			this.loadTexture(pack, entry, zis);
			this.loadSound(pack, entry, zis);
			if (entry.getName().endsWith(".json")) {
				byte[] data = this.readData(pack, entry, zis); // コンフィグとみなして読み込んで
				if (data != null) {
					// 何かしらのデータが取得できた場合はコンフィグ読み取りをするリストに追加
					configs.add(new String(data, StandardCharsets.UTF_8)); // Stringデータとして格納
				}
			}
		}

		// コンフィグデータを解析して登録
		this.loadConfig(pack, configs);
		return pack;
	}

	/**
	 * エントリーのバイナリデータを全部読み取り、Byte配列にした状態で返す。
	 * 何らかの都合で正常に読み込めなかった場合、その理由をログに出力して
	 * nullを返す。
	 *
	 * @param pack パック。
	 * @param entry エントリー。
	 * @param zis ZIPのInputstream。
	 * @return 読み込んだデータ。失敗した場合はnull。
	 */
	private byte[] readData(Pack pack, ZipEntry entry, ZipInputStream zis) {
		try {
			byte[] data = new byte[(int) entry.getSize()]; // 読み込んだ結果を格納する配列
			int read = 0; // どこまで読み込んだかを判定するための変数
			while (read < entry.getSize()) {
				read += zis.read(data, read, (int)(entry.getSize() - read)); // 一度には読み込めないので読みだせる分を読んでいく
			}
			return data;
		} catch (IOException e) {
			// そもそもファイルがおかしい場合
			GTS.LOGGER.warn("["+pack.getName()+"] entry \""+entry.getName()+"\" cannot be opened. -> " + e.getMessage());
		}
		return null;
	}

	/**
	 * MQOファイルを読み取り、パックに登録する。
	 * 壊れているなどの場合は追加を行わない。
	 *
	 * @param pack  追加したいパック。
	 * @param entry 追加するモデルエントリー。
	 */
	private void loadModel(Pack pack, ZipEntry entry, ZipInputStream zis) {
		if (!entry.getName().endsWith(".mqo")) return; // ファイル名からして異なる場合
		if (pack.getModels().containsKey(entry.getName())) return; // 既に登録されている場合
		byte[] data = this.readData(pack, entry, zis); // データ取得して
		if (data == null) return; // 不正ならバイバイ
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			MQO mqo = new MQO(bais);
			pack.getModels().put(entry.getName(), mqo); // 内容を追加
		} catch (IOException e) {
			GTS.LOGGER.warn("["+pack.getName()+"] entry \""+entry.getName()+"\" is not a MQO model. -> " + e.getMessage());
		}
		GTS.LOGGER.info("["+pack.getName()+"] entry \""+entry.getName()+"\" loaded.");
	}

	/**
	 * テクスチャとして使用する画像ファイルを読み取り、パックに登録する。
	 * 壊れているなどの場合は追加を行わない。
	 *
	 * @param pack 追加したいパック。
	 * @param entry エントリー。
	 * @param zis ZipファイルのInputStream。
	 */
	private void loadTexture(Pack pack, ZipEntry entry, ZipInputStream zis) {
		if (!entry.getName().endsWith(".png") && !entry.getName().endsWith(".jpg")) return; // ファイル名からして異なる場合
		if (pack.getTextures().containsKey(entry.getName())) return; // 既に登録されている場合
		byte[] data = this.readData(pack, entry, zis);
		if (data == null) return; // 読み込みに失敗した場合
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			BufferedImage bi = ImageIO.read(bais);
			pack.getTextures().put(entry.getName(), bi);
		} catch (IOException e) {
			GTS.LOGGER.warn("["+pack.getName()+"] entry \""+entry.getName()+"\" is invalid texture file. -> " + e.getMessage());
		}
		GTS.LOGGER.info("["+pack.getName()+"] entry \""+entry.getName()+"\" loaded.");
	}

	/**
	 * oggサウンドを読み込み、登録を行う。
	 * 壊れている場合は登録されない。
	 * @param pack パック。
	 * @param entry エントリー。
	 * @param zis ZipInputStream。
	 */
	private void loadSound(Pack pack, ZipEntry entry, ZipInputStream zis) {
		if (!entry.getName().endsWith(".ogg")) return; // ファイル名からして異なる場合
		if (pack.getSounds().containsKey(entry.getName())) return; // 既に登録されている場合
		byte[] data = this.readData(pack, entry, zis);
		if (data == null) return; // 読み込みに失敗した場合
		pack.getSounds().put(entry.getName(), data);
		GTS.LOGGER.info("["+pack.getName()+"] entry \""+entry.getName()+"\" loaded.");
	}

	/**
	 * あらかじめ読み込んでおいたコンフィグ一覧を解析し、適切なコンフィグに変換する。
	 * いずれにもできない場合は登録せず放棄する。
	 *
	 * TODO: コンフィグの具象クラスが完成したらここに処理を追記していく
	 * @param pack パック。
	 * @param configs コンフィグデータの一覧。
	 */
	private void loadConfig(Pack pack, ArrayList<String> configs) {
		GTS.LOGGER.info("["+pack.getName()+"] "+configs.size()+" configs detected. Parsing...");
		Gson gson = new Gson(); // JSONを読み込むためのオブジェクト
		ProgressManager.ProgressBar bar = ProgressManager.push("Load Config", configs.size());
		for (String config: configs) {
			// 1回ずつコンフィグを回していく
			bar.step("a");
		}
		ProgressManager.pop(bar); // 終了
		GTS.LOGGER.info("["+pack.getName()+"] "+pack.getConfigs().size()+" configs loaded.");
	}
}
