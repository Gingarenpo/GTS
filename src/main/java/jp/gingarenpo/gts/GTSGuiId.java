package jp.gingarenpo.gts;

public enum GTSGuiId {
	TRAFFIC_CONTROLLER(1),
	MODEL_CHOOSER(11),
	TRAFFIC_SIGN(2);

	/**
	 * IDとして格納しておくためのフィールド。
	 */
	private final int id;

	/**
	 * コンストラクタは外部からはアクセスできないが
	 * 定数を自前で生成するために宣言する必要がある
	 * @param id GUIのID。
	 */
	private GTSGuiId(int id) {
		this.id = id;
	}

	public static GTSGuiId value(int id) {
		for (GTSGuiId g: GTSGuiId.values()) {
			if (g.id == id) return g;
		}
		return null;
	}

	/**
	 * 逆引きとして、EnumからIDを返す。
	 * @return その定数のID。
	 */
	public int getId() {
		return id;
	}

}
