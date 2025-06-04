# 交通信号機

最終更新日時：2025年6月4日

交通信号機（以下、信号機とします）は、以下のクラスにより構成されています。

- `GTSBlockTrafficLight` → ブロックインスタンス
- `GTSTileEntityTrafficLight` → 信号機のTileEntity
- `GTSTileEntityRendererTrafficLight` → 信号機のレンダラー
- `GTSTrafficLightConfig` → 信号機の個別情報（パック）

## パックJSON（コンフィグ）の仕様

信号機は、共通の項目以外に以下の設定が必要です。

|設定項目名称|概要|備考|
|---|---|---|
|light|この信号機において、点灯するオブジェクトの一覧。文字列の配列で指定します。|ここを設定することで、フェーズ状態において無点灯のものをしっかり描画できるようになります。|


### textures

信号機の点灯部分、非点灯部分を別のテクスチャとして描画することにより、よりリアリティの増した描画ができるようにしています。そのため、以下の追加項目の設定が必要です。

|設定項目名称|概要|備考|
|---|---|---|
|light|信号機の点灯部分に関するテクスチャのパス。|全て同一で構わない場合は、baseと同じパスを入れても構いません。|
|noLight|信号機の非点灯部分に関するテクスチャのパス。|全て同一で構わない場合は、baseと同じパスを入れても構いません。|

### patterns

交通信号制御機のチャンネルに指定することができるものとして指定する必要があります。ここには、この交通信号機の現示パターンを配列で指定します。配列内には、そのパターンで点灯させるモデルのオブジェクト名を指定することになります。後方互換性のため現在未使用の項目もありますが、設定をしてください。

|設定項目名称|概要|備考|
|---|---|---|
|name|パターン名|半角英数字64文字以内推奨。|
|object|このパターンで点灯させるオブジェクト名を列挙する。|文字列の配列で指定します。|
|ticks|点滅周期（Tick）。指定した周期で点滅を繰り返すようになります。なお、点灯時間・非点灯時間はそれぞれここで指定したTickの半分の時間となります。|0を指定すると常時点灯になります。負の数を指定しないでください。バグります。|

### 例

```json
{
    "model": "models/Light_CommonSteel.mqo",
    "textures": {
        "base": "NSS1_base.png",
        "light": "NSS1_light.png",
        "noLight": "NSS1_nolight.png"
    },
    "body": [
        "body",
        "g",
        "overg",
        "y",
        "overy",
        "r",
        "overr",
        "body_back",
        "g_back",
        "overg_back",
        "y_back",
        "overy_back",
        "r_back",
        "overr_back"
    ],
    "patterns": [
        {
            "name": "green",
            "objects": [
                "g300"
            ],
            "tick": 0
        },
        {
            "name": "yellow",
            "objects": [
                "y300"
            ],
            "tick": 0
        },
        {
            "name": "red",
            "objects": [
                "r300"
            ],
            "tick": 0
        },
        {
            "name": "yellow_flush",
            "objects": [
                "y300"
            ],
            "tick": 20
        },
        {
            "name": "red_flush",
            "objects": [
                "r300"
            ],
            "tick": 20
        },
        {
            "name": "green",
            "objects": [
                "g300",
                "g300_back"
            ],
            "tick": 0
        },
        {
            "name": "yellow",
            "objects": [
                "y300",
                "y300_back"
            ],
            "tick": 0
        },
        {
            "name": "red",
            "objects": [
                "r300",
                "r300_back"
            ],
            "tick": 0
        },
        {
            "name": "yellow_flush",
            "objects": [
                "y300",
                "y300_back"
            ],
            "tick": 20
        },
        {
            "name": "red_flush",
            "objects": [
                "r300",
                "r300_back"
            ],
            "tick": 20
        },
        {
            "name": "green_yellow",
            "objects": [
                "g300",
                "y300_back"
            ],
            "tick": 0
        },
        {
            "name": "green_red",
            "objects": [
                "g300",
                "r300_back"
            ],
            "tick": 0
        },
        {
            "name": "yellow_green",
            "objects": [
                "y300",
                "g300_back"
            ],
            "tick": 0
        },
        {
            "name": "red_green",
            "objects": [
                "r300",
                "g300_back"
            ],
            "tick": 0
        },
        {
            "name": "redF_yellowF",
            "objects": [
                "r300",
                "y300_back"
            ],
            "tick": 20
        },
        {
            "name": "yellowF_redF",
            "objects": [
                "y300",
                "r300_back"
            ],
            "tick": 20
        }
    ],
    "size": 1.5,
    "opacity": 0.5,
    "light": [
        "g300",
        "y300",
        "r300",
        "g300_back",
        "y300_back",
        "r300_back"
    ],
    "id": "NSS1_2H33GYR_over"
```

なんかかぶっているものもありますが概ねこのような形で記載します。

## チャンネルについて

交通信号制御機の欄で詳しく解説しますが、信号機はチャンネルを持つことができます。そのチャンネル名をもとに、アタッチされた制御機から信号を受信し、現示を行います。
