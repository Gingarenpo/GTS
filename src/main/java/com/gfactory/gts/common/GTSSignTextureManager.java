package com.gfactory.gts.common;

import com.gfactory.gts.minecraft.GTS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 地名板を自動生成する（文字とか入れて）場合、
 * 単純に色々な地名板をやみくもに作ると直ぐにメモリが使い果たしてしまう。
 * それを避けるため、同じ地名板の場合はキャッシュを活用して
 * メモリを最小限に抑えるためのマネージャー。
 * このクラス自体はシングルトンとなっている。
 */
public class GTSSignTextureManager {

    /**
     * シングルトンスタイルのため、インスタンスを自分自身で格納しておく
     */
    private static GTSSignTextureManager instance;

    /**
     * 114対応地名板のキャッシュ格納場所
     */
    private final HashMap<GTS114Sign, ResourceLocation> sign114 = new HashMap<>();

    /**
     * オリジナルのBufferedImageも保持しておく
     */
    private final HashMap<GTS114Sign, BufferedImage> sign114Original = new HashMap<>();

    /**
     * スレッドの実行状況を格納している管理マップ
     */
    private final ConcurrentHashMap<GTS114Sign, Future<BufferedImage>> pendingTasks = new ConcurrentHashMap<>();

    /**
     * テクスチャは最大4つのスレッドで進行する
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * 初回インスタンス作成時に必ず作成する、デフォルトテクスチャ
     */
    public static final GTS114Sign PLACE_HOLDER_INFO = new GTS114Sign();

    public static ResourceLocation PLACE_HOLDER;

    private GTSSignTextureManager() {
        this.createThread(PLACE_HOLDER_INFO, true);
    }

    /**
     * インスタンスを取得する
     * @return インスタンス
     */
    public static GTSSignTextureManager getInstance() {
        if (instance == null) instance = new GTSSignTextureManager();
        return instance;
    }

    private void createThread(GTS114Sign info, boolean dummy) {
        // 作成を開始するため、新しくスレッドを追加する
        Callable<BufferedImage> task = () -> this.createTexture(info);
        Future<BufferedImage> future = executor.submit(task);

        // 実行中としてタスクを記録
        this.pendingTasks.put(info, future);

        // 完了を検出し、タスクの消去などを行うスレッドを定義
        executor.submit(() -> {
            try {
                BufferedImage result = future.get();
                if (result != null) {
                    // 既に完了していた場合
                    synchronized (this.sign114Original) {
                        this.sign114Original.put(info, result); // キャッシュを格納
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            // OpenGLの利用はメインスレッドからでないとできないため、こちらで登録を行う
                            ResourceLocation r = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(info.toString(), new DynamicTexture(result));
                            if (!dummy) {
                                synchronized (this.sign114) {
                                    this.sign114.put(info, r);
                                }
                            }
                            else {
                                PLACE_HOLDER = r;
                            }
                        });
                    }
                }
            } catch (Exception e) {
                // 何かしらの例外を拾った場合は通知を出す
                GTS.LOGGER.warn(I18n.format("gts.exception.texture.create", e.getLocalizedMessage()));
            } finally {
                // 失敗しようが成功しようが最終的にはタスクを実行終了するのでキューから消す
                pendingTasks.remove(info);
            }
        });
    }

    /**
     * 指定した地名板情報を用いて、キャッシュされたテクスチャの中から探す。
     * もしあれば、それを返す。ない場合は作成を試みる。
     *
     * @param info 地名板の情報
     * @return その地名板を作成、あるいはキャッシュしたテクスチャのResourceLocation。ない場合はプレースホルダー
     */
    public ResourceLocation getResourceLocation(GTS114Sign info) {
        synchronized (this.sign114) {
            // キャッシュを同期し、その中に既にテクスチャがあれば返す
            if (this.sign114.containsKey(info)) {
                return this.sign114.get(info);
            }
        }

        if (!pendingTasks.containsKey(info)) this.createThread(info, false);

        // 一応デフォルトを探し、あればそれを返す
        return PLACE_HOLDER;
    }

    /**
     * 指定した地名板の原本データを返す。ない場合は作る。nullになることもある。
     * @param info
     * @return
     */
    public BufferedImage getBufferedTexture(GTS114Sign info) {
        if (this.sign114.containsKey(info)) return this.sign114Original.get(info);
        this.createTexture(info);
        return this.sign114Original.get(info);
    }

    /**
     * 指定した地名板情報を用いて、テクスチャを作成する。
     * このテクスチャは地名板のテクスチャに適合された状態で保存される。
     * 解像度はそこまで高くないものにしているため近すぎると死ぬ
     * @param info 地名板の情報
     * @return 作成したBufferedImage。作成できなかった場合はnull
     */
    private BufferedImage createTexture(GTS114Sign info) {
        // 幅固定か幅合わせで地名板の大きさが変わる
        // 一旦現時点では幅固定のみを対象とする
        int res = 512; // 解像度（1）

        // フォントを探す（ない場合はフォールバックする）
        ArrayList<String> fonts = GTSSignTextureManager.getAvailableFonts();
        Font japaneseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 200);
        if (fonts.contains(info.japaneseFont)) japaneseFont = new Font(info.japaneseFont, Font.PLAIN, 200);
        Font englishFont = new Font(Font.SANS_SERIF, Font.PLAIN, 100);
        if (fonts.contains(info.englishFont)) englishFont = new Font(info.englishFont, Font.BOLD, 100);
        // 文字幅を取得する
        FontRenderContext jpFrc = new FontRenderContext(null, true, true);
        FontRenderContext enFrc = new FontRenderContext(null, true, true);
        TextLayout jpTl = new TextLayout(info.japanese, japaneseFont, jpFrc);
        TextLayout enTl = new TextLayout(info.english, englishFont, enFrc);
        float jpWidth = jpTl.getAdvance() / info.japanese.length(); // 1文字あたり
        float enWidth = enTl.getAdvance();

        res = 520; // 寸法がこれなので一旦これで作成
        int MARGIN = 20; // 枠線とのマージン
        int borderWidth = 10; // 枠線の太さ
        int paddingWidthEdge = 70; // 枠線から日本語部分が始まるまでのパディング
        int paddingHeightEdge = 60; // 高さ
        int paddingText = 40; // 字間
        int jpEnGap = 40; // 日本語と英語のギャップ
        float enScale = 0.8f; // 初期英字縮小率
        float jpScale = 1.0f; // 初期日本語縮小率
        int width = 0;
        int jpPadding = 0; // 幅固定の場合に顕著で文字数が余り散らかしている場合の余白

        if (info.widthFix) {
            // 幅は固定
            width = (int) (res * info.aspect);
            // 日本語の縮小判定
            if ((paddingText * (info.japanese.length() - 1) + jpWidth * info.japanese.length()) * jpScale > width - MARGIN * 2 - borderWidth * 2 - paddingWidthEdge * 2) {
                // オーバーする場合、若干詰める
                paddingWidthEdge = 35; // 枠線から日本語部分が始まるまでのパディング
                paddingText = 20;
                jpScale = (width - MARGIN * 2 - borderWidth * 2 - paddingWidthEdge * 2) / (paddingText * (info.japanese.length() - 1) + jpWidth * info.japanese.length());
            }
            else {
                // ピッタリってことはそうそうないと思うが、それ以下の場合は余白を埋めなくてはならない
                // 正直めんどくさいので、全角スペースで対応するとしてこちらではPaddingを両端に入れる
                float gap = Math.abs((paddingText * (info.japanese.length() - 1) + jpWidth * info.japanese.length()) * jpScale - (width - MARGIN * 2 - borderWidth * 2 - paddingWidthEdge * 2));
                jpPadding = (int) (gap / 2.0f);
            }
        }
        else {
            // 文字数分の幅、文字数-1分のギャップ、枠線x2、マージンx2、内部パディングx2で幅が決まる
            width = (info.japanese.length() * 200) + (info.japanese.length() - 1) * paddingText + borderWidth * 2 + MARGIN * 2 + paddingWidthEdge * 2;
        }
        // 英字部分の縮小判定
        if (enWidth * enScale > width - MARGIN * 2 - borderWidth * 2 - paddingWidthEdge * 2) {
            enScale = (width - MARGIN * 2 - borderWidth * 2 - paddingWidthEdge * 2) / enWidth;
        }

        // この大きさで背景色の地名板オリジナルテクスチャを作成
        BufferedImage image = new BufferedImage(width, res, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = image.createGraphics();
        AffineTransform origin = g.getTransform();
        // 各種設定（アンチエイリアシングとか）
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 背景色で塗りつぶし
        g.setColor(info.color);
        g.fillRect(0, 0, width, res);
        // 枠線を描画
        g.setColor(info.textColor);
        g.setStroke(new BasicStroke(borderWidth));
        g.drawRoundRect(MARGIN, MARGIN, width - MARGIN * 2, res - MARGIN * 2, MARGIN, MARGIN);
        // 日本語を1文字ずつ描画
        g.setFont(japaneseFont);
        g.scale(jpScale, 1.0);
        int y = (int) (MARGIN + borderWidth + paddingHeightEdge + jpTl.getAscent() + jpTl.getDescent());
        for (int i = 0; i < info.japanese.length(); i++) {
            char text = info.japanese.charAt(i);
            int x = (int) (jpPadding + (MARGIN / jpScale + borderWidth / jpScale + paddingWidthEdge / jpScale + i * paddingText + i * 200));
            g.setStroke(new BasicStroke(1));
            g.drawString(String.valueOf(text), x, y);
        }
        g.setTransform(origin);
        // 英字部分を描画
        // 英字部分は中央ぞろえで描画することになるので、Xを求める
        // ただし、英字部分は0.8倍するのでそれを考慮する
        // 収まりきらない場合はX方向に限界まで潰す
        g.scale(enScale, 1.0); // スケールを変更
        int x = (int) ((width / 2.0 - enWidth * enScale / 2.0) / enScale); // 開始地点を算出できる
        y = (MARGIN + borderWidth + paddingHeightEdge + 200 + paddingText + 100);
        g.setFont(englishFont);
        enTl.draw(g, x, y);
        g.setTransform(origin); // 戻す

        // このテクスチャを1024x1024のテクスチャに落とし込む
        // ただし、余白として10%残す必要があるため、922px四方に落とし込む
        // 表裏を考えて配置する
        BufferedImage resizeImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D rg = resizeImage.createGraphics();
        rg.setColor(info.color);
        rg.fillRect(0, 0, 1024, 1024);
        rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        rg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        rg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rg.drawImage(image, 0, 0, 922, 461, null);

        // ダイナミックテクスチャを作成
        return resizeImage;
    }


    /**
     * 現在利用可能なフォントの一覧を取得する
     * @return　利用可能なフォント
     */
    public static ArrayList<String> getAvailableFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return new ArrayList<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
    }

    public static void main(String[] args) {
        GTSSignTextureManager manager = GTSSignTextureManager.getInstance();
        GTS114Sign info = new GTS114Sign();
        info.widthFix = true;
        manager.createTexture(info);
    }

    /**
     * 114-A・Bの地名板に対応した地名板の情報
     * 色とか枠線とか角丸とかその辺を入れている。
     */
    public static class GTS114Sign {
        /**
         * 日本語部分
         */
        public String japanese = "生成中";

        /**
         * 英字部分
         */
        public String english = "Generating";

        /**
         * 地名板の色
         */
        public Color color = new Color(255, 255, 255);

        /**
         * 地名板のフォントの色
         */
        public Color textColor = new Color(0, 0, 255);

        /**
         * 地名板の日本語部分のフォント
         */
        public String japaneseFont = "A-SK Nar Min2 E";

        /**
         * 地名板の英語部分のフォント
         */
        public String englishFont = "Helvetica Neue";

        /**
         * 地名板は角丸か？（trueにすると山梨みたいな感じになる）
         */
        public boolean rounded = false;

        /**
         * 地名板は枠線があるか？
         */
        public boolean border = true;

        /**
         * この地名板は幅固定か？
         */
        public boolean widthFix = true;

        /**
         * 高さを1としたときの幅の割合。デフォルトは2。幅固定の場合のみこの値を参照する。
         */
        public double aspect = 2.0;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GTS114Sign)) return false;
            GTS114Sign that = (GTS114Sign) o;
            return rounded == that.rounded && border == that.border && widthFix == that.widthFix && Double.compare(aspect, that.aspect) == 0 && Objects.equals(japanese, that.japanese) && Objects.equals(english, that.english) && Objects.equals(color, that.color) && Objects.equals(textColor, that.textColor) && Objects.equals(japaneseFont, that.japaneseFont) && Objects.equals(englishFont, that.englishFont);
        }

        @Override
        public int hashCode() {
            return Objects.hash(japanese, english, color, textColor, japaneseFont, englishFont, rounded, border, widthFix, aspect);
        }

        public void readFromNBT(NBTTagCompound compound) {
            this.japanese = compound.getString("japanese");
            this.english = compound.getString("english");
            this.japaneseFont = compound.getString("japanese_font");
            this.englishFont = compound.getString("english_font");
            this.color = new Color(compound.getInteger("color"));
            this.textColor = new Color(compound.getInteger("text_color"));
            this.aspect = compound.getDouble("aspect");
            this.widthFix = compound.getBoolean("width_fix");
            this.rounded = compound.getBoolean("rounded");
            this.border = compound.getBoolean("border");
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("japanese", japanese);
            compound.setString("english", english);
            compound.setString("japanese_font", japaneseFont);
            compound.setString("english_font", englishFont);
            compound.setInteger("color", color.getRGB());
            compound.setInteger("text_color", textColor.getRGB());
            compound.setDouble("aspect", aspect);
            compound.setBoolean("width_fix", widthFix);
            compound.setBoolean("rounded", rounded);
            compound.setBoolean("border", border);

            return compound;
        }
    }
}
