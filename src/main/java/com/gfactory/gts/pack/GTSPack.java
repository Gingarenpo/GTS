package com.gfactory.gts.pack;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOException;
import com.gfactory.core.mqo.MQOLoader;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.config.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ProgressManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * <h1>GTS拡張パッククラス</h1>
 * <p>パックの実体はこの中に格納されている。バイトストリームの形で格納されているので、たくさんのアドオンがあるとそれだけメモリを消費するので注意。</p>
 * <h2>概要</h2>
 * <p>ZIPファイルから読み込んだデータを以下の種類に分けてそれぞれ保持している。</p>
 * <ul>
 *     <li>コンフィグファイル（JSON）　……　後述するデータを紐づけて管理するための設定ファイル</li>
 *     <li>モデルファイル（MQO）　……　モデリングデータそのもの</li>
 *     <li>テクスチャファイル（PNG、JPG、BMP…）　……　モデリングデータなどに使用するテクスチャファイル。Javaで読み込める者は大抵読み込む。</li>
 *     <li>音声ファイル（OGG）　……　音声として再生できる有効なデータ。OGGのみ。ちょっとひと工夫必要。</li>
 * </ul>
 * <h2>gts.txtについて</h2>
 * <p>メタ情報を詰め込んだテキストファイル。この名前でないと読み込まない。必ずZIPファイルの直下におくこと。</p>
 * <p>JavaのPropertiesと似た感じの扱いになっており、キー=値として管理する。一部のキーは予約されており、それ以外はメタデータとして独自情報が
 * 保持されるが正直あまり使いどころがない。</p>
 * <ul>
 *     <li>name: パックの名前。被ってもよいがこの名前が至る所に使われるので被ると利用者が困る。デフォルトはZIPファイル名の拡張子を省いたもの。入れとくべき。</li>
 *     <li>author: パック制作者名。クレジットとして入れておくべき。GUIで出すかどうかは考えていない。</li>
 * </ul>
 *
 * <h2>サウンドファイルについて</h2>
 * <p>サウンドファイルは、一度リソースパックの形にしてから出ないと読み込むことができない。</p>
 * <p>しかしリソースパックを都度展開するのも地獄なので、各パックごとに動的なリソースパックを用いてそれを読み込むようにする。</p>
 */
public class GTSPack {

    /**
     * サウンドデータをバイト列でそのまま保持する。
     */
    private final HashMap<String, byte[]> sounds = new HashMap<>();

    /**
     * サウンドデータをもとに登録したサウンドイベントの一覧
     */
    private final HashMap<String, SoundEvent> soundEvents = new HashMap<>();

    /**
     * テクスチャデータをBufferedImageでラップする。DynamicTextureとして使用するため。
     */
    private final HashMap<String, BufferedImage> textures = new HashMap<>();

    /**
     * Minecraft内で使用することのできる動的テクスチャを作成する。
     * 動的テクスチャをレンダリングのたびに作るとメモリが爆発するので
     * 最初の1回だけ生成し、あとは使いまわす。
     */
    private final HashMap<String, ResourceLocation> bindTextures = new HashMap<>();

    /**
     * モデルデータを格納する。
     * ここにあるのは原寸大で、サイズが違う場合はそれに比例して増える。それは仕方がない。
     */
    private final HashMap<String, MQO> models = new HashMap<>();

    /**
     * リサイズされたデータを格納する。ここに関しては分割するしかなかったんです。
     */
    private final HashMap<Double, HashMap<String, MQO>> resizedModels = new HashMap<>();

    /**
     * コンフィグデータを保持する。ベースであるGTSConfigで保持するが、実際の中身としては
     * 実装されたコンフィグデータが入ることになる。
     */
    private final HashMap<String, GTSConfig<GTSConfig.GTSTexture>> configs = new HashMap<>();

    /**
     * GTS.txtから取得できるパックの名前。存在しない場合はファイル名の拡張子を抜いたもの。
     * NULLになると色々困るため、初期値として「UNKNOWN」を入れておいている。
     */
    private String name = DUMMY;

    /**
     * GTS.txtから取得できるパックの制作者。存在しない場合はやはりUNKNOWN。
     */
    private String author = DUMMY;

    /**
     * その他メタ情報。将来的にバージョンが変わればもしかしたら別フィールドとして独立するかもしれないが、
     * そうなっても互換性が保てるようにnameとかauthorもここから拾えるようにしてある。あれば。
     */
    private final HashMap<String, String> metas = new HashMap<>();

    /**
     * このパックの実ロケーション。パスを指定して格納する。NULLの場合、ダミーパックとして扱う。
     */
    private File file;

    /**
     * ダミーパックインスタンス
     */
    private static GTSPack dummyPack;

    /**
     * 完全にモデルを使用しないものでダミーモデル（小さな立方体）
     */
    public static final String DUMMY = "dummy";

    /**
     * ダミーモデルの定数（交通信号機モデル）
     */
    public static final String DUMMY_TRAFFIC_LIGHT = "traffic_light";

    /**
     * ダミーモデルの定数（交通信号制御機モデル）
     */
    public static final String DUMMY_TRAFFIC_CONTROLLER = "traffic_controller";

    /**
     * ダミーモデルの定数（ポールモデル）
     */
    public static final String DUMMY_TRAFFIC_POLE = "traffic_pole";

    /**
     * ダミーモデルの定数（アームモデル）
     */
    public static final String DUMMY_TRAFFIC_ARM = "traffic_arm";

    /**
     * ダミーモデルの定数（アームモデル）
     */
    public static final String DUMMY_TRAFFIC_BUTTON = "traffic_button";

    private GTSPack() {

    }

    /**
     * このパックが空データかどうかを判定する。
     * このメソッドがtrueを返した場合は大抵の場合碌なデータではないので読み込むとクラッシュするだろう。
     *
     * @return あらゆるデータが存在しない場合はtrue
     */
    public boolean empty() {
        return (this.sounds.isEmpty() && this.textures.isEmpty());
    }

    /**
     * このパックがダミーデータかどうかを判定する。
     * ダミーデータの場合はfileがNULLになっているのでそれで判別できる。
     * @return ダミーデータの場合はtrue
     */
    public boolean dummy() {
        return (this.file == null);
    }


    /**
     * ZIPファイルの中身からPackを読み込み、そのPackのインスタンスを返す。
     * 基本的に例外が発生しない限りNULLになることはないが、不正なパックの場合何も登録されていないことがある。
     * その際は、<code>GTSPack.empty()</code>を実行すると判別できる。
     * 音声ファイルのサウンドイベントの追加はローダーが行うようにしている。
     * @param zis 読み込むZIPファイルのインスタンス
     * @param file 読み込むZIPファイルのファイルオブジェクト。これをそのままパックに登録する。
     * @return 読み込んだPack。
     * @throws IllegalArgumentException fileやzisがNULLの場合
     * @throws IOException Zip内のファイル読み込みに失敗した場合
     */
    public static GTSPack load(ZipInputStream zis, File file) throws IllegalArgumentException, IOException {
        if (zis == null || file == null) throw new IllegalArgumentException(I18n.format("gts.exception.argument"));
        // 0. 毎度作っていたらきりがないので定数の準備
        Pattern p = Pattern.compile("^(.+?)=(.*)$");
        GTSPack pack = new GTSPack();
        pack.file = file;

        // 0-2. ファイル数を把握するためZipFileインスタンスも作成。これはすぐ開放する
        ZipFile zfile = new ZipFile(file);
        ProgressManager.ProgressBar bar = ProgressManager.push("Pack file loading", zfile.size());
        zfile = null;

        // 1. ZIPファイル内のエントリごとに繰り返す
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            GTS.LOGGER.debug(entry.getName());
            bar.step(entry.getName());
            // 一旦バイナリファイルを読み込み
            byte[] data = readData(entry, zis);

            if (entry.getName().equals("gts.txt")) {
                // 中身を読み込む（Propertiesにすることができないので自前で）
                try (Scanner s = new Scanner(new String(data, StandardCharsets.UTF_8))) {
                    while (s.hasNextLine()) {
                        String line = s.nextLine();
                        Matcher m = p.matcher(line);
                        if (!m.matches()) continue;
                        // キーと値で読み込み
                        String key = m.group(1);
                        String value = m.group(2);
                        // メタデータに追加
                        pack.metas.put(key, value);

                        // フィールドセット
                        if (key.equals("name")) {
                            // 名前をセットする
                            pack.name = value;
                        }
                        else if (key.equals("author")) {
                            // 作成者をセットする
                            pack.author = value;
                        }
                    }
                }
            }

            else if (entry.getName().endsWith(".png")
                    || entry.getName().endsWith(".PNG")
                    || entry.getName().endsWith(".jpg")
                    || entry.getName().endsWith(".JPG")
            ) {
                // 画像の場合、BufferedImageを作成してそのインスタンスを格納する
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                    BufferedImage texture = ImageIO.read(bais);
                    pack.textures.put(entry.getName(), texture);
                } catch (IOException e) {
                    // 拡張子だけ変えて実は読み込めないとかそんな感じの場合
                    GTS.LOGGER.warn(I18n.format("gts.exception.pack_load.texture", entry.getName()));
                }
            }

            else if (entry.getName().endsWith(".ogg")) {
                // 音声ファイルの場合は一旦信じてバイト列として格納
                pack.sounds.put(entry.getName(), data);
            }

            else if (entry.getName().endsWith(".json")) {
                // JSONファイルだと信じて、コンフィグの読み込みを試す
                try {
                    GTSConfig config = GTSPack.readConfig(data);
                    pack.configs.put(entry.getName(), config);
                } catch (RuntimeException e) {
                    // -
                    e.printStackTrace();
                }
            }

            else if (entry.getName().endsWith(".mqo")) {
                // MQOファイルの読み込みを行うが、InputStreamしか対応していない（めんどくさい）ので
                // ByteArrayInputStreamにして読み込む
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                    MQO mqo = MQOLoader.load(data).normalize(1.0); // 一旦作ってもらい、それを格納する
                    GTS.LOGGER.debug(mqo);
                    pack.models.put(entry.getName(), mqo);
                } catch (IOException | MQOException e) {
                    // 何らかのエラーの場合
                    GTS.LOGGER.warn(I18n.format("gts.exception.pack_load.model", entry.getName()) + " -> " + e.getLocalizedMessage());
                }
            }
        }

        ProgressManager.pop(bar);

        return pack;
    }

    /**
     * ダミーパックを返す。
     * ダミーパックはこのMod内にあるダミーモデルとダミーコンフィグを読み込み、それを元にしたパックとして提供する。
     * 既にワールドに設置されているが何らかの影響で読み込めない場合等に、このダミーパックが使用される。
     * また、ワールドに初めに設置したときはこのダミーパックが設置される。ダミーパックの仕様は変わるため、
     * 余り依存しない方がいい。なお、ダミーパックはメモリの使用量の関係からシングルトンとする。
     * @return
     */
    public static GTSPack getDummyPack() {
        if (dummyPack != null) {
            return dummyPack;
        }

        // パックを作成
        GTSPack pack = new GTSPack();

        // モデルを追加（ここ落ちるかもしれないけど）
        try {
            pack.models.put(GTSPack.DUMMY, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/dummy.mqo")));
            pack.models.put(GTSPack.DUMMY_TRAFFIC_LIGHT, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/trafficlight.mqo")));
            pack.models.put(GTSPack.DUMMY_TRAFFIC_CONTROLLER, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/trafficcontroller.mqo")));
            pack.models.put(GTSPack.DUMMY_TRAFFIC_POLE, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/trafficpole.mqo")));
            pack.models.put(GTSPack.DUMMY_TRAFFIC_ARM, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/trafficarm.mqo")));
            pack.models.put(GTSPack.DUMMY_TRAFFIC_BUTTON, MQOLoader.load(GTS.class.getResourceAsStream("/assets/gts/dummy/trafficbutton.mqo")));
        } catch (IOException | MQOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }

        // テクスチャを追加（ここも）
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/dummy.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/trafficlight.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY_TRAFFIC_LIGHT, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/trafficcontroller.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY_TRAFFIC_CONTROLLER, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/trafficpole.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY_TRAFFIC_POLE, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/trafficarm.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY_TRAFFIC_ARM, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/dummy/trafficbutton.png")) {
            if (is == null) throw new IOException();
            pack.textures.put(GTSPack.DUMMY_TRAFFIC_BUTTON, ImageIO.read(is));
        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }

        // 音声ファイルを追加（なぜかogg拡張子を必ずつけないとCodec Errorになる）
        try (InputStream is = GTS.class.getResourceAsStream("/assets/gts/sounds/traffic_button.ogg")) {
            if (is == null) throw new IOException();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096]; // 4KBずつ読み込み
                int readByte = 0;
                while ((readByte = is.read(data, 0, data.length)) != -1) {
                    baos.write(data, 0, readByte);
                }
                pack.sounds.put(GTSPack.DUMMY_TRAFFIC_BUTTON + ".ogg", baos.toByteArray());
            }

        } catch (IOException e) {
            // ダミーファイルが読み込めない場合は落とす（この先壊れるから）
            throw new RuntimeException("[ERROR] Cannot load dummy model on GTS!");
        }

        GTS.LOGGER.info(I18n.format("gts.message.pack_search.dummy"));
        return pack;
    }

    /**
     * 指定したZIPファイル内のエントリを実際に読み込み、バイト配列として格納する。
     * 読み出せない場合はログにその旨を出力し空のバイト列等を返す。
     *
     * <hr>
     *
     * 補足：ZipEntryは順番に並んでいるのでサイズ取得して読み取っていけばシーケンシャルに読み取れる
     * @param entry 読み込むファイルのエントリ。
     * @param zis 読み込むファイルのストリーム。
     * @return 読み込んだバイト列。NULLにはならんが全部0になっていることはある
     */
    private static byte[] readData(ZipEntry entry, ZipInputStream zis) {
        byte[] data = new byte[(int) entry.getSize()];
        try {
            int read = 0;
            while (read < entry.getSize()) {
                read += zis.read(data, read, (int) (entry.getSize() - read)); // 直ぐに全部呼び出すことができないためWhileでガンバ
            }
        } catch (IOException e) {
            // ファイルの読み込みに失敗した場合
            GTS.LOGGER.warn(I18n.format("gts.exception.pack_load.io", entry.getName(), e.getLocalizedMessage()));
        }
        return data;
    }

    /**
     * 指定したZIPファイルのエントリのバイト列を利用して、その中身をもとにJSON読み込みを試す。
     * 存在する全クラスを試すが、今はここを手打ちしている。どうにかしたい。
     * GTSConfigを返すが、無効なJSONの場合はNULLを投げるので注意。
     *
     * @param data readData等を使用して読み込んだバイト列
     */
    private static GTSConfig readConfig(byte[] data) {
        // 0. GSONインスタンスを作成し、コンフィグの読み込みを準備する
        Gson gson = new Gson();

        // 1. とにかく回す
        try {
            GTSTrafficButtonConfig config = gson.fromJson(new String(data, StandardCharsets.UTF_8), GTSTrafficButtonConfig.class);
            if (config.getAudios() == null || config.getAudios().getBase() == null) throw new JsonSyntaxException("");
        } catch (JsonSyntaxException e) {
            try {
                GTSTrafficArmConfig config = gson.fromJson(new String(data, StandardCharsets.UTF_8), GTSTrafficArmConfig.class);
                if (config.getBaseObjects().isEmpty()) throw new JsonSyntaxException("");
                return config;
            } catch (JsonSyntaxException e2) {
                try {
                    GTSTrafficPoleConfig config = gson.fromJson(new String(data, StandardCharsets.UTF_8), GTSTrafficPoleConfig.class);
                    if (config.getNormalObject() == null) throw new JsonSyntaxException("");
                    return config;
                } catch (JsonSyntaxException e3) {
                    // JSONとして不正な場合
                    try {
                        GTSTrafficLightConfig config = gson.fromJson(new String(data, StandardCharsets.UTF_8), GTSTrafficLightConfig.class);
                        if (config.getLight() == null) throw new JsonSyntaxException("");
                        return config;
                    } catch (JsonSyntaxException e4) {
                        try {
                            return gson.fromJson(new String(data, StandardCharsets.UTF_8), GTSTrafficControllerConfig.class);
                        } catch (JsonSyntaxException e5) {
                            // JSONとして不正な場合
                            GTS.LOGGER.warn("エラーだ！");
                        }
                    }
                }
            }
        }

        return null;
    }


    /**
     * このパックの文字列表現を返す。見やすい形にして返すが基本的に仕様が定まっているわけではないのであくまでデバッグ用。
     * インスタンスID振られるよりはマシってだけ。
     * @return このパックの文字列表現
     */
    @Override
    public String toString() {
        return
                "GTSPack: [name=" +
                        this.name +
                        ", author=" +
                        this.author +
                        ", meta_count=" +
                        this.metas.size() +
                        ", texture_count=" +
                        this.textures.size() +
                        ", sound_count=" +
                        this.sounds.size() +
                        ", model_count=" +
                        this.models.size() +
                        "]";
    }

    public HashMap<String, byte[]> getSounds() {
        return sounds;
    }

    public HashMap<String, BufferedImage> getTextures() {
        return textures;
    }

    public HashMap<String, MQO> getModels() {
        return models;
    }

    public HashMap<String, GTSConfig<GTSConfig.GTSTexture>> getConfigs() {
        return configs;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public HashMap<String, String> getMetas() {
        return metas;
    }

    /**
     * バインドされたテクスチャを返す。なければ作る。
     * @param name 名前
     * @return リソースロケーション
     */
    public ResourceLocation getOrCreateBindTexture(String name) {
        ResourceLocation r = this.bindTextures.get(name);
        BufferedImage b = this.textures.get(name);
        if (r != null) return r;

        // ない場合は、動的テクスチャを作成してそのリソースロケーションを保持する
        if (b == null) {
            return null; // そもそもテクスチャがない場合はnull
        }
        r = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, new DynamicTexture(b));
        this.bindTextures.put(name, r);

        return r;
    }

    /**
     * リサイズされたモデルを返す。ない場合はリサイズを行う。リサイズは重い処理なのである場合は省略することで得をする。
     * @param name 名前
     * @param size リサイズされたサイズ
     * @return あればそれ、なければ作る、モデルそのものがない場合はNULL
     */
    public MQO getResizingModels(String name, double size) {
        // 指定したサイズに切り詰められたモデルを返す。切り詰め作業が重すぎるので基本はインスタンスを使いまわす
        HashMap<String, MQO> h = this.resizedModels.get(size);
        if (h == null) {
            h = new HashMap<String, MQO>();
            this.resizedModels.put(size, h);
        }
        MQO m = h.get(name);
        if (m == null) {
            // 存在しないので作る
            MQO o = this.models.get(name);
            if (o == null) {
                return null; // ない場合はもうむりなのでNULL
            }
            o = o.normalize(size);
            h.put(name, o);
            return o;
        }
        return m;
    }

    public HashMap<String, SoundEvent> getSoundEvents() {
        return soundEvents;
    }
}
