package com.gfactory.gts.pack;

import com.gfactory.gts.minecraft.GTS;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 * GTS内で使用するPackに紐づき、そのサウンドファイルを登録するために用意する
 * オンザメモリのリソースパック。
 * Minecraftにサウンドを認識してもらうためにはここを利用することしかできない。
 * そのため、sounds.jsonを動的に作成し、それを読み込ませ、リソースパックとして振舞ってもらう。
 * 読み取り専用となる。
 *
 * RAMの使用量がどれくらい増えるのか要検討
 */
public class GTSMemoryResourcePack implements IResourcePack {

    /**
     * このリソースパックが使用する為のGTS拡張パック。
     * この中にあるリソースを読み込みに行く。なおテクスチャとかも入るが
     * ここではオーディオのみ読み込む想定。将来的にどうするかは未定。
     */
    private final GTSPack pack;

    private String soundJson;

    public GTSMemoryResourcePack(GTSPack pack) {
        this.pack = pack;
        this.createSoundJson();
    }

    private void createSoundJson() {
        // このパックの中身をもとに、sounds.jsonを作成する
        // JSONの書き方が特殊なので（ルートキーがもはや可変）、GSONを使えない
        // そのためちまちま文字列で追加していくことになる
        StringBuilder sb = new StringBuilder();
        sb.append("{"); // JSONの始まりを示す物を追加
        for (String path: this.pack.getSounds().keySet()) {
            // これをキーとして登録
            sb.append("\"").append(this.getPackName()).append(":").append(path).append("\": {");
            // カテゴリ
            sb.append("\"category\": \"block\",");
            // sounds配列を開始
            sb.append("\"sounds\": [{");
            // サウンドの名前を入れる
            sb.append("\"name\": \"").append(this.getPackName()).append(":").append(path).append("\",");
            // サウンドのタイプを入れる
            // sb.append("\"type\": \"file\",");
            // サウンドの確率らしいがまあ適当に
            sb.append("\"weight\": 100,");
            // リソースパックのロード時にサウンドを読み込むか
            sb.append("\"preload\": true");
            // soundsオブジェクト終わり、
            sb.append("}]");
            // このキー終わり
            sb.append("},");
        }
        if (!this.pack.getSounds().isEmpty()) {
            sb.deleteCharAt(sb.length()-1);// ラストのカンマはいらないので消して
        }
        // JSONを終わらせる
        sb.append("}");

        // これをsoundJsonとする
        this.soundJson = sb.toString();
    }

    /**
     * リソースロケーションが指定された場合に、その実体となるデータを返す。
     * 存在しない場合はExceptionを投げるため、戻り値はnullであってはならない。
     * @param location 参照されるリソースロケーション
     * @return そのリソースロケーションが指し示すファイルの実体となるデータ
     * @throws IOException ファイルが見つからない場合など
     */
    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        // ロケーションは、「gts_パック名:相対パス」で渡してくることを想定している
        // こうしないと他のModと競合する
        // なので相対パスをもとにファイルを探す
        if (this.pack.getSounds().containsKey(location.getResourcePath())) {
            // サウンドの中にあればそれを返す
            return new ByteArrayInputStream(this.pack.getSounds().get(location.getResourcePath()));
        }
        else if (location.getResourcePath().equals("sounds/sounds.json")) {
            // sounds.jsonは生成した物を返す
            return new ByteArrayInputStream(this.soundJson.getBytes(StandardCharsets.UTF_8));
        }

        // 見つからないため例外
        throw new FileNotFoundException(location.toString());
    }

    /**
     * リソースロケーションで指定されたファイルが存在するかどうかを返す
     * @param location リソースロケーション
     * @return あればtrue、なければfalse
     */
    @Override
    public boolean resourceExists(ResourceLocation location) {
        GTS.LOGGER.error(location);
        if (!location.getResourceDomain().equals(this.getPackName())) {
            return false;
        }
        if (location.getResourcePath().equals("sounds/sounds.json")) {
            return true;
        }
        return this.pack.getSounds().containsKey(location.getResourcePath());
    }

    /**
     * このリソースのドメインを返す
     * @return ドメイン（gts_パック名）
     */
    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("gts_" + this.pack.getName());
    }

    /**
     * なんかメタデータがあれば返すものだが、特に存在しないためメタデータは返さない
     * @param metadataSerializer ？
     * @param metadataSectionName ？
     * @return 何もないためNULL
     * @param <T> ？
     * @throws IOException なぜか返す
     */
    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    /**
     * このリソースパックのアイコンを返すが存在しないためnull
     * @return アイコン画像（サムネイル）だが存在しないため常にNULL
     * @throws IOException
     */
    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    /**
     * このリソースパックの名前を返す。
     * 使うことはないがgts_一応パック名を返す
     * @return
     */
    @Override
    public String getPackName() {
        return "gts_" + this.pack.getName();
    }

}
