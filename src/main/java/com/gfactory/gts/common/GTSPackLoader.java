package com.gfactory.gts.common;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.client.resources.I18n;
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
     * 指定されたパス内にあるZIPファイルを読み込み、それがGTSで使用可能なパックであれば
     * 中身を読み取ろうとする。読み取りに成功した場合はこのインスタンスに登録され、アクセスできる。
     * このメソッドは、Preinitで呼び出されることを想定している。
     *
     * @param file 検索を行うパスを入力。nullも入るが、nullの場合は検索せずに終了するため意味がない。
     */
    public void searchPacks(File file) {
        if (file == null) return;

        GTS.LOGGER.info(I18n.format("gts.message.pack_search.start", file.getAbsolutePath()));

        // 1-1. 配下にあるZIPファイルを検索。ZIPファイルは拡張子のみで一旦識別を行う。
        File[] files = file.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files == null) {
            // そもそも見つからん
            GTS.LOGGER.info(I18n.format("gts.message.pack_search.notfound"));
            return;
        }
        GTS.LOGGER.info(I18n.format("gts.message.pack_search.zip_count", files.length));

        // 1-2. アドオン読み込み用のプログレスバーを取得する。
        ProgressManager.ProgressBar bar = ProgressManager.push("GTS Pack Search", files.length);

        // 2. ZIPファイルを読み込んでパックを抽出していく
        for (File f: files) {
            bar.step(f.getName());
            // 2-1. Zipファイルとして読み込んでみる
            try (FileInputStream fis = new FileInputStream(f)) {
                try (ZipInputStream zis = new ZipInputStream(fis)) {
                    // 2-2. ZIPファイルの中身を読み込んで、それをPackに格納する
                    GTSPack pack = this.load(zis, f);
                    GTS.LOGGER.debug(pack.toString());
                    if (pack.empty()) {
                        // 不正がありそうなファイルの場合はクラッシュを避けるため読み込みを避ける
                        GTS.LOGGER.warn(I18n.format("gts.message.pack_search.error.empty", f.getName()));
                        continue;
                    }
                    this.packs.add(pack);
                }
            } catch (IOException e) {
                // 2-X: ZIPファイルとして不正なもの、壊れているものである場合、その旨をログに出力して続行
                GTS.LOGGER.error(I18n.format("gts.message.pack_search.error.zip", f.getName()));
            }
        }

        // 3. 後始末とかは任せたぞ
        ProgressManager.pop(bar);
        GTS.LOGGER.info(I18n.format("gts.message.pack_search.finish", this.packs.size()));
    }

    /**
     * 指定したファイルロケーションに存在するパックを読み込み、Packインスタンスを作成して返す
     *
     * @param zis 読み込むべきZIPファイルのZIS
     * @param file 読み込むべきZIPファイルのファイルオブジェクトそのもの
     * @return 読み込んだ結果のPackファイル
     */
    private GTSPack load(ZipInputStream zis, File file) throws IOException {
        GTS.LOGGER.info(I18n.format("gts.message.pack_load.start", file.getName()));
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
