package com.gfactory.gts.common;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.ProgressManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

/**
 * <h1>GTS Pack Loader</h1>
 * <p>モデルパックやその他カスタマイズパッケージを読み込むためのローダー。</p>
 * <h2>概要</h2>
 * <p>定められたパス内にあるZIPファイルをチェックし、有効なPackである場合はそれを追加する。</p>
 * <p>GTS1.0ではローダーインスタンスが全てのパックの中身を保持していたが、重複するモデルも全てカウントしてしまう。
 * そのため、メモリ不足に陥ることが多かった。したがって、ここではパックのインスタンスのみを保持する。</p>
 * <p>その都合上、パックを跨いだモデルの使用は原則できない。</p>
 *
 * <hr>
 *
 * <p>モデルパックをゲーム中に再読み込みすることができるようにalpha6で少し改良。ゲーム内でインスタンスを置き換えるのはかなりデンジャラスなので、
 * 最悪ダミーに置き換わってしまわないように一度ローダーインスタンスをもう一度作ってから素のインスタンスと入れ替える形で使用する。</p>
 * <p>NBTタグに関してはパックの名称で判断しているのでこちらもそのように対処。念のため、パックの同一性判定も修正。</p>
 *
 * @author Gingarenpo
 *
 */
public class GTSPackLoader {
    /**
     * ローダーが読み込んだ全てのパックの一覧。
     */
    private final ArrayList<GTSPack> packs = new ArrayList<GTSPack>();

    /**
     * パックローダーを初期化する。
     */
    public GTSPackLoader() {
        // ダミーパックを追加する
        packs.add(GTSPack.getDummyPack());
    }

    /**
     * 指定したロケーションからZipファイルを見つけて返す。
     * @param location 探すもととなるパス
     * @return 中で見つかったファイル一覧。そもそもない場合はnullがかえることがある
     */
    private File[] findZip(File location) {
        return location.listFiles((dir, name) -> name.endsWith(".zip"));
    }

    /**
     * 該当するパスに存在するZipファイルの読み込みを試みる。余り設計としては好ましくないが、このメソッドはパックの読み取り結果によって以下の状態になる。
     * <ul>
     *     <li>パックの読み込みに成功した場合: true</li>
     *     <li>パックとしての読み込みには成功したものの、不正なファイルだらけでパックとして認識されない場合: false</li>
     *     <li>そもそもZipファイルですらない、中身が壊れているなどして読み込みができなかった場合: IOExceptionをスロー</li>
     * </ul>
     *
     * @param f 読み込もうとするZipファイル
     * @return 上記説明を参照
     * @throws IOException 上記説明を参照
     */
    private boolean loadZip(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            try (ZipInputStream zis = new ZipInputStream(fis)) {
                GTSPack pack = this.load(zis, f);
                GTS.LOGGER.debug(pack.toString());
                if (pack.empty()) return false;
                this.packs.add(pack);
                return true;
            }
        }
    }

    /**
     * 指定されたパス内にあるZIPファイルを読み込み、それがGTSで使用可能なパックであれば
     * 中身を読み取ろうとする。読み取りに成功した場合はこのインスタンスに登録され、アクセスできる。
     * reloadを有効にすると、ゲーム内でのリロードになる。プログレスバーや同一性判定などが追加される。
     *
     * @param file 検索を行うパスを入力。nullも入るが、nullの場合は検索せずに終了するため意味がない。
     */
    public void searchPacks(File file) {
        if (file == null) return;

        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.start", file.getAbsolutePath()));

        // Zipファイルの検索
        File[] files = this.findZip(file);
        if (files == null) {
            // そもそも見つからん
            GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.notfound"));
            return;
        }
        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.zip_count", files.length));

        // アドオン追加用プログレスバーの読み込み
        ProgressManager.ProgressBar bar = ProgressManager.push("GTS Pack Search", files.length);

        // 各Zipファイルのロード
        for (File f: files) {
            bar.step(f.getName());
            // 2-1. Zipファイルとして読み込んでみる
            try  {
                if (!this.loadZip(f)) {
                    // 読み込みに失敗した場合
                    GTS.LOGGER.warn(GTSI18n.i18n("gts.message.pack_search.error.empty", f.getName()));
                }
            } catch (IOException e) {
                // 2-X: ZIPファイルとして不正なもの、壊れているものである場合、その旨をログに出力して続行
                GTS.LOGGER.error(GTSI18n.i18n("gts.message.pack_search.error.zip", f.getName()));
            }
        }

        // オーディオ登録
        GTS.proxy.registerResourcePack(this.packs);

        // 後始末とかは任せたぞ
        ProgressManager.pop(bar);
        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.finish", this.packs.size()));
    }

    /**
     * インスタンスが新たに作成されていることを前提として、現在のインスタンスにおいてパックの「リロード」を行う。
     * リロード時と初回読み込み時の違いとしては、進捗状況（Infoに相当）をsenderのチャット欄に表示するというもの。
     * ICommandSenderはコマンドを送信できるインターフェースとして用いられるため、プレイヤーを入れてもいい。
     * なお、サーバーとクライアントで少し違うことも考慮して一応isremoteの結果も渡すようにする。
     *
     * @param file 検索を開始する対象となるパス
     * @param sender 実行者。クライアントが実行した場合はプレイヤー、サーバーが実行した場合はサーバーのチャット。
     * @param isRemote world.isRemote参照。
     */
    public void reloadPacks(File file, ICommandSender sender, boolean isRemote) {
        if (file == null) return; // 壊れているので無視

        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.start", file.getAbsolutePath()));
        sender.sendMessage(new TextComponentTranslation("gts.message.pack_search.start", file.getAbsolutePath()));

        // Zipファイルの検索
        File[] files = this.findZip(file);
        if (files == null) {
            // そもそも見つからん
            GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.notfound"));
            return;
        }
        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.zip_count", files.length));

        // 各Zipファイルのロード
        for (File f: files) {
            // 2-1. Zipファイルとして読み込んでみる
            try  {
                if (!this.loadZip(f)) {
                    // 読み込みに失敗した場合
                    GTS.LOGGER.warn(GTSI18n.i18n("gts.message.pack_search.error.empty", f.getName()));
                }
            } catch (IOException e) {
                // 2-X: ZIPファイルとして不正なもの、壊れているものである場合、その旨をログに出力して続行
                GTS.LOGGER.error(GTSI18n.i18n("gts.message.pack_search.error.zip", f.getName()));
            }
        }

        // オーディオの登録……はリロード先で行う（落ちる）

        // 後始末とかは任せたぞ
        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_search.finish", this.packs.size()));
        sender.sendMessage(new TextComponentTranslation("gts.message.pack_search.finish", this.packs.size()));
    }

    /**
     * 指定したファイルロケーションに存在するパックを読み込み、Packインスタンスを作成して返す
     *
     * @param zis 読み込むべきZIPファイルのZIS
     * @param file 読み込むべきZIPファイルのファイルオブジェクトそのもの
     * @return 読み込んだ結果のPackファイル
     */
    private GTSPack load(ZipInputStream zis, File file) throws IOException {
        GTS.LOGGER.info(GTSI18n.i18n("gts.message.pack_load.start", file.getName()));
        GTSPack p = GTSPack.load(zis, file);
        GTS.LOGGER.debug(p.toString());
        return p;
    }

    /**
     * 指定した名前のパックを探し、そのパックを返す。見つからない場合はNULLを返す。完全一致のみ。
     * @param name パックの名前。
     * @return パックがあればそのパック、なければNULL
     */
    public GTSPack getPack(String name) {
        for (GTSPack pack: this.packs) {
            if (pack.getName().equals(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * ダミーパックを返す。ただし、正常に読み込まれていない場合はNULLを返す。
     * @return ダミーパック
     */
    public GTSPack getDummy() {
        if (this.packs.isEmpty()) return null;
        GTSPack p = this.packs.get(0);
        return (p.dummy()) ? p : null;
    }

    /**
     * このローダーが読み込んだパックの一覧を取得する。
     * @return パック一覧
     */
    public ArrayList<GTSPack> getPacks() {
        return packs;
    }

}
