package jp.gingarenpo.gts.pack;

import jp.gingarenpo.gingacore.mqo.MQO;
import jp.gingarenpo.gts.base.ConfigBase;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

/**
 * 追加アドオンパック単位でのデータ格納場所。
 * 以前は各コンフィグでそのたびに自動生成してメモリをバカ食いしていたが、
 * 今回は同じパック内で同じテクスチャなどを使用している場合にデータを共有するように
 * 変更した。
 * 基本的にGTSのパックはこのクラスを使用するが、RTMからコンバートしたパックも継承して
 * 使用できるようにするかもしれない。
 */
public class Pack {

	/**
	 * oggデータ（生バイナリ）を保持するフィールド。
	 * キーにZIPルートからの相対ロケーションが書かれており、この内容を元に中身を取り出す。
	 * サウンドイベントとしてはこのままでは登録できないが、パック内で使用する
	 * 音源を自動的に登録するために予約している。
	 */
	private HashMap<String, byte[]> sounds = new HashMap<>();

	/**
	 * メモリバカ食いの一番の原因でもあったモデルを保持するフィールド。
	 * MQOインスタンス1つ1つがそれなりに重いインスタンスなので、なるべく共通化する方針をとる。
	 * 読み込みを行うときは、MQOファイルを直に参照しに行く。
	 */
	private HashMap<String, MQO> models = new HashMap<>();

	/**
	 * テクスチャを保持するフィールド。こちらも共有リソースとして保持する。
	 * Renderに渡すときにはこの中身を保持する。
	 */
	private HashMap<String, BufferedImage> textures = new HashMap<>();

	private HashMap<String, ConfigBase> configs = new HashMap<>();

	/**
	 * このパックの名前。GTSのパックである場合、パックのinfoにそれが記されている。
	 * しかし、このファイルがなくても扱うことはある。その場合、ファイル名を使用する。
	 * デフォルトは「Unknown」として、nullにならないようにする。
	 */
	private String name;

	/**
	 * ファイルロケーション。このパックの実体が何処に存在するかを確認するためのパスを格納する。
	 * パックが存在しているかどうかに使用する。
	 */
	private File file;

	/**
	 * 指定された名前と、指定されたロケーションを用いて、パックのインスタンスを生成する。
	 * @param name パックの名前。
	 * @param file パックのファイルロケーション。
	 */
	public Pack(String name, File file) {
		if (name == null || file == null) {
			throw new IllegalArgumentException("Cannot create pack because some arguments is null!");
		}
		this.name = name;
		this.file = file;
	}

	/**
	 * 名前は未定義のパックを作成する。ファイルロケーションのみを指定する。
	 * この場合、パックの名前はunknownになるので注意。通常はこのコンストラクタは使用しない。
	 * @deprecated 未定義の名前でのパックは予期せぬ誤動作を発生させる可能性があるため、基本的に使用は非推奨。
	 * @param file パックのファイルロケーション。
	 */
	public Pack(File file) {
		this("unknown", file);
	}

	/**
	 * メソッドが呼び出された時点において、このパックは指定されたロケーションに存在しているかどうかを返す。
	 * 中身のチェックまでは行わない。中身の確認を行いたい場合は、<code>empty</code>メソッドを用いる。
	 *
	 * @return 指定したロケーションにファイルが存在していればtrue、していなければfalse。
	 */
	public boolean isExist() {
		return file.exists();
	}

	/**
	 * メソッドが呼び出された時点において、このパックが空（＝利用不可）であるかどうかを返す。
	 * パックとして有効な形式ではない場合、このパックには何もデータが登録されていないことになる。
	 * その場合、パックとして有効ではない。なお、メソッド名の通り「有効でない」場合にtrueを返すので注意。
	 *
	 * @return 有効でないパックの場合true、そうでないならばfalse。
	 */
	public boolean empty() {
		return this.models.isEmpty() && this.sounds.isEmpty() && this.textures.isEmpty();
	}

	/**
	 * 登録されているサウンドの一覧を返す。
	 * @return サウンド。
	 */
	public HashMap<String, byte[]> getSounds() {
		return sounds;
	}

	/**
	 * サウンドをセットする。
	 * @param sounds 登録したいサウンド。
	 */
	public void setSounds(HashMap<String, byte[]> sounds) {
		this.sounds = sounds;
	}

	/**
	 * 登録されているモデル一覧を返す。
	 *
	 * @return モデル。
	 */
	public HashMap<String, MQO> getModels() {
		return models;
	}

	/**
	 * モデルを登録する。重複チェックは行われない。
	 * @param models 登録したいモデル。
	 */
	public void setModels(HashMap<String, MQO> models) {
		this.models = models;
	}

	/**
	 * 登録されているテクスチャ一覧を返す。
	 *
	 * @return テクスチャ。
	 */
	public HashMap<String, BufferedImage> getTextures() {
		return textures;
	}

	/**
	 * テクスチャを登録する。
	 * @param textures 登録したいテクスチャ。
	 */
	public void setTextures(HashMap<String, BufferedImage> textures) {
		this.textures = textures;
	}

	/**
	 * このパックの名前を返す。最初に値が与えられていない場合、unknownが現時点では帰る仕様になっているが
	 * この動作は変更される場合があるので依存したコードは書かないこと。
	 * @return このパックの名前。
	 */
	public String getName() {
		return name;
	}

	/**
	 * パックの名前を新たに指定する。nullや空文字を入れるとIllegalArgumentExceptionを発生させる。
	 * @param name 登録したい名前。
	 * @throws IllegalArgumentException nameがnullや空文字である場合
	 */
	public void setName(String name) throws IllegalArgumentException {
		if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot null or empty!");
		this.name = name;
	}

	/**
	 * パックのロケーションを返す。
	 * @return パックのロケーション。
	 */
	public File getFile() {
		return file;
	}

	/**
	 * パックのロケーションを登録する。nullでもエラーは発生しないがおすすめはしない。
	 * @param file 登録したいパックのロケーション。
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * パックのコンフィグ一覧を返す。
	 * @return コンフィグ。
	 */
	public HashMap<String, ConfigBase> getConfigs() {
		return configs;
	}

	/**
	 * パックのコンフィグを登録する。
	 * @param configs コンフィグ一覧。
	 */
	public void setConfigs(HashMap<String, ConfigBase> configs) {
		this.configs = configs;
	}
}
