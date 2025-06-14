# アーム

最終更新日時：2025年6月4日

追加バージョン：2.0 alpha2

アームは、以下のクラスにより構成されています。

- **`GTSItemTrafficArm`** → アームのアイテムインスタンス
- `GTSTileEntityTrafficArm` → アームのTileEntity
- `GTSTrafficArmConfig` → アームの個別情報（パック）

なお、他のクラスとの相違点として、以下があります。

- ブロックとして存在しません。アームは、ポールの一種となります。そのため、ブロックインスタンスではなく**アイテムインスタンス**となります。
- アームはポールと一緒に描画されるため、`TileEntitySpecialRenderer`は存在しません。
- ブロックとして存在しない以上TileEntityの実態もないため、`GTSTileEntityTrafficArm`はダミーのTileEntityとなっています。すなわち、Minecraftに登録されているTileEntityではありません。

他と違いクラスが分かりにくいと思いますが、上記にしたがって閲覧いただければと思います。

## パックJSON（コンフィグ）の仕様

アームは、共通の項目以外に以下の設定が必要です。

|設定項目名称|概要|備考|
|---|---|---|
|edgeObjects|ポール始点から描画されるアームのオブジェクトを記載します。||
|baseObjects|中間地点で描画されるアームのオブジェクトを記載します。|後述しますがここは必須で、割と重要なポイントです。|
|endObjects|ゴールとなるポイントで描画されるアームのオブジェクトを記載します。|たいていの場合はベースと同じかと思われます|
|drawStartPrimary|`true` / `false`で指定します。`true`にすると、長さが足りない場合でもedgeObjectsの描画を優先します。`false`にすると、足りない場合に描画がされなくなります。|`true`を通常は指定してください。`false`は特殊な用途でのみ使用します。|

なお、アームでは`object`項目は使用しませんが、後方互換性のためにnormalObjectと同様のものを入れておくことを推奨します。

`drawStartPrimary`に関しては、後方互換性のために入れているものとなります。通常は`true`を指定してください。コードを読んで何をやっているのか理解できる方は`false`に変更しても構いません。


### 例

```json
{
        "id": "Arm_Normal2_Ped",
        "model": "models/Arm_Ped2Normal.mqo",
        "textures": {
                "base": "arm.png"
        },
        "edgeObjects": ["start"],
        "baseObjects": ["base"],
        "endObjects": ["end"],
        "size": 1,
        "drawStartPrimary": false
}
```

## モデルの作り方

アームはその性質上、モデルの作り方に若干の癖があります。これを守ることでよりスムーズに描画がされるので、できるだけ従ってください。コードを読んで描画内容を理解できる方は色々カスタマイズされても構いません。

### 描画の仕組み

アームの描画は、以下のようにして行われます。

1. ポールの開始地点ブロックの中心を原点とし、接続先の方向に`size`で指定した分だけの長さで`edgeObjects`を描画します。
2. `edgeObjects`の描画終了地点から、接続先地点のブロックから`size`で指定した分の長さを引いた長さに`baseObjects`を**スケールして**描画します。
3. 最後に、接続先地点のブロックから`size`で指定した分の長さを引いた地点から接続先地点までを、`endObjects`によって描画します。

特に重要なのが2で、`baseObjects`はスケーリングされて描画されます。そのため、しっかり意識して作成しないと変な伸び方をしたりします。

### `baseObjects`の制約

`baseObjects`は、**原点を中心としてX軸方向にスケーリングされます**。したがって、アームの作成時は必ずモデルを原点に配置し（とくにX座標）、X軸方向にアームを伸ばしてください。この時のサイズは`size`になりますのでそれも意識してください。

### その他の制約

ポールと同様に、全て原点を中心に作成してください。