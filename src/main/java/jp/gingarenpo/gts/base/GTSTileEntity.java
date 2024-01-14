package jp.gingarenpo.gts.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * GTS内で使用するTileEntityの共通クラス。
 * GTS2.0では、全てのTileEntityをIDで管理するため、その共通実装を抽出したものとなる。
 * Tには、このTileEntityが持ち合わせるべき実体を代入する。モデルパックの中身自体は
 * Configを別途持たせる形で保持するため、このTでは固有のデータを参照する場合に
 * 使用されたい。
 */
public abstract class GTSTileEntity<T extends NBTConvertible<T>, U extends ConfigBase> extends TileEntity {

	/**
	 * このTileEntityのデータをNBTタグに格納する際のキー。
	 */
	public static final String NBT_KEY = "gts_data";

	/**
	 * このTileEntityが保持しているデータの実体。
	 * TileEntity自体は決められた座標に対してその内容をS/Cで相互にやり取りするための
	 * クラスであり、データの差し替えに関しては考慮しない。
	 */
	protected T data;

	/**
	 * このTileEntityの外観を決めるコンフィグデータの実体。
	 * 実体と言ってもこの中にリソースバイナリが入っているわけではなく、
	 * ようはConfigのインスタンスの使いまわし。
	 */
	protected U config;

	/**
	 * データが何も存在しない空のTileEntityを作成する場合に使用する。
	 * ワールドが生成された際に始めて作られ、サーバーからパケットを受信すると
	 * そのデータに置き換わる。
	 */
	public GTSTileEntity() {
		// NO CODE
	}

	/**
	 * データを指定してTileEntityを生成する。通常このコンストラクタは使われることがないが、
	 * 空を作成してからsetDataを実行するのがめんどくさい場合にこちらを使用することができる。
	 * @param data 登録すべきデータ。
	 */
	public GTSTileEntity(T data) {
		this.data = data;
	}

	/**
	 * タグの内容からデータを作成し、取り込むために使用する抽象メソッド。
	 * タグの内容を元にして、Tを生成する実装を行うこと。
	 * ファクトリメソッドとして別途クラスを別だしするべきだとは思うが、
	 * 管理が大変になるのでこちらで行う。
	 *
	 * @param compound クラスの情報が詰まっていると思われるタグ。
	 * @return クラスとしてインスタンスを作れる場合、そのインスタンス。作れない場合はNULL。
	 */
	public abstract T createData(NBTTagCompound compound);

	/**
	 * 登録されているデータの実体を返す。
	 * @return データ。
	 */
	public T getData() {
		return data;
	}

	/**
	 * データをセットする。nullを指定した場合、データが存在しないものとして扱う。
	 * @param data 登録したいデータ。
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * このTileEntityが抜け殻であるかどうかを確認するためのメソッド。
	 * @return データが登録されていないか、明示的に削除された場合にtrue。
	 */
	public boolean empty() {
		return this.data == null;
	}

	/**
	 * このTileEntityのコンフィグを取得する。
	 * @return コンフィグ
	 */
	public U getConfig() {
		return config;
	}

	/**
	 * このTileEntityのコンフィグを登録する。
	 * @param config 登録したいコンフィグ。
	 */
	public void setConfig(U config) {
		this.config = config;
	}

	/**
	 * 渡されたNBTタグを解析し、タグに記載されている中身をフィールドに格納する。
	 * パケット通信はこのNBTTagで渡されるため、デシリアライズするために必要とされる。
	 *
	 * @param compound 受信したりディスクから読み込んだNBTタグ。
	 */
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey(NBT_KEY)) {
			if (this.data != null) {
				// データの種類自体は変更されないので、Tが既に存在する場合は最新の状態を反映する
				this.data.readFromNBT(compound.getCompoundTag(NBT_KEY));
			}
			else {
				// NULLの場合、タグをもとにしてデータを作成する
				this.data = this.createData(compound);
			}
		}
	}

	/**
	 * 渡されたNBTタグに対して、現在のインスタンスの状況をNBTタグに書き込む。
	 * パケット通信はこのNBTTagを使用してやり取りされるため、シリアライズを行うために必要。
	 * 全て書き込んだらそのタグを返す。
	 *
	 * @param compound 追記すべきタグ。
	 * @return 追記すべきものをし終わった、処理完了後のタグ。
	 */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);

		if (this.data != null) {
			// データの内容を書き込む（データが存在する場合のみ）
			nbt.setTag(NBT_KEY, this.data.writeToNBT(new NBTTagCompound()));
		}

		return nbt;
	}

	/**
	 * サーバーからクライアントに対して、現在の状況を共有するためのパケットを返す。
	 * TileEntity専用のパケットクラスが用意されており、内部ではNBTタグを保管している。
	 * そのNBTタグを指定させて、送信するためのパケットを整備する。SPacketの第二引数は不明。
	 *
	 * @return 更新用に使用するパケット。
	 */
	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, -1, this.getUpdateTag());
	}

	/**
	 * 更新用のデータ（NBTタグ）自体を作成するために必要なメソッド。
	 * このメソッドが呼び出されると、更新用パケットの作成時に使われるデータが返される。
	 * writeToNBTを呼び出しておけばいいが、更新時に必要のないフィールドなどを退避させておくと
	 * パケットの節約につながる。
	 *
	 * @return 更新用パケットを生成するために必要なNBTタグ。
	 */
	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	/**
	 * パケットがサーバーからクライアントに送信されてきたとき、何をするかについてのイベントリスナー。
	 * netはどのようなパケットの種類なのかを格納しており？、実際にはパケットとしてgetUpdateParketで
	 * 生成されたパケットが送られてくる。それ以外のパケットには対応していないらしい？
	 *
	 * @param net ネットワークマネージャー。
	 * @param pkt 受信したパケット。
	 */
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound()); // 中身のデータを取得して読み込み、反映させる
	}
}
