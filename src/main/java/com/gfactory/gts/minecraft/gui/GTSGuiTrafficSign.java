package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.common.sign.GTS114Sign;
import com.gfactory.gts.common.sign.GTSSignBase;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.sign.GTSGui114Sign;
import com.gfactory.gts.minecraft.gui.widget.GTSGuiScrollList;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficSignConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public abstract class GTSGuiTrafficSign<T extends GTSSignBase> extends GTSGui<GTSTileEntityTrafficSign> {

    /**
     * 日本語部分
     */
    protected GuiTextField japanese;

    /**
     * 英語部分
     */
    protected GuiTextField english;

    /**
     * 文字色・枠色
     */
    protected GuiTextField foreGroundColor;

    /**
     * 背景色
     */
    protected GuiTextField backGroundColor;

    /**
     * 日本語フォント
     */
    protected GuiTextField japaneseFont;

    /**
     * 英語フォント
     */
    protected GuiTextField englishFont;

    /**
     * 設置する標示板の幅
     */
    protected GuiTextField signWidth;

    /**
     * 設置する標示板の高さ
     */
    protected GuiTextField signHeight;

    /**
     * 設置する標示板の奥行
     */
    protected GuiTextField signDepth;

    protected GuiCheckBox use114Sign;

    /**
     * メッセージ（なんかいろいろ書くところ）
     */
    protected String message;

    /**
     * 苦肉の策で、地名板情報のクラスインスタンス
     */
    protected Class<T> clazz;

    /**
     * テクスチャを選択するリスト
     */
    protected GTSGuiScrollList textureScrollList;

    /**
     * 実際にテクスチャを格納するリスト（重いのでGUI起動時の初めの1回のみ）
     */
    protected TreeMap<String, GTSTrafficSignConfig> choices;

    /**
     * テクスチャ検索用
     */
    protected GuiTextField modelSearch;

    /**
     * パックキャッシュ
     */
    protected GTSPack cache;

    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficSign(GTSTileEntityTrafficSign tileEntity, Class<T> clazz) {
        super(tileEntity);
        this.clazz = clazz;

        this.cache = this.getTextureChoices();

    }

    private GTSPack getTextureChoices() {
        GTSTrafficSignConfig teConfig = (GTSTrafficSignConfig) this.tileEntity.getConfig();
        GTSPack resPack = null;
        this.choices = new TreeMap<>();
        for (GTSPack pack: GTS.LOADER.getPacks()) {
            if (pack.dummy()) continue; // ダミーパックはスルー
            // パックの中身を読み込み
            HashMap<String, BufferedImage> textures = pack.getTextures();
            for (Map.Entry<String, BufferedImage> entry: textures.entrySet()) {
                String name = pack.getName() + ":" + entry.getKey();
                if (this.modelSearch != null && !this.modelSearch.getText().isEmpty() && !name.contains(this.modelSearch.getText())) continue; // この選択肢は出さない
                GTSTrafficSignConfig config = new GTSTrafficSignConfig();
                config.setDummy();
                config.setTexture(entry.getKey());
                this.choices.put(pack.getName() + ":" + entry.getKey(), config);
                if (Objects.equals(this.tileEntity.getConfig().getTextures().getBase(), entry.getKey())) {
                    // 一致する場合、それを指し示すパックを返してあげるために代入
                    resPack = pack;
                }
            }
        }
        return resPack;
    }

    @Override
    public void initGui() {
        super.initGui();
        // 地名板の各情報
        this.addButton(new GuiButton(
                1,
                this.width / 2 + MARGIN,
                this.height / 2 - 20 - MARGIN,
                this.width / 2 - MARGIN * 2,
                20,
                I18n.format("gts.gui.sign.apply")
        ));

        // 使えるフォントを確認する方法（暫定的）
        this.addButton(new GuiButton(
                2,
                MARGIN,
                this.height / 2 + MARGIN * 4 + fontRenderer.FONT_HEIGHT * 4,
                this.width / 2 - MARGIN * 2,
                20,
                I18n.format("gts.gui.sign.check.font.available")
        ));

        this.use114Sign = new GuiCheckBox(
                3,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT + MARGIN * 2,
                I18n.format("gts.gui.sign.use114sign"),
                false
        );

        this.japanese = new GuiTextField(
                11,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 5 + MARGIN * 6,
                (this.width / 2 - MARGIN * 3) / 2,
                fontRenderer.FONT_HEIGHT
        );
        this.english = new GuiTextField(
                12,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 4,
                fontRenderer.FONT_HEIGHT * 5 + MARGIN * 6,
                (this.width / 2 - MARGIN * 3) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.foreGroundColor = new GuiTextField(
                13,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 3 + MARGIN * 4,
                (this.width / 3 - MARGIN * 4) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.backGroundColor = new GuiTextField(
                15,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6,
                fontRenderer.FONT_HEIGHT * 3 + MARGIN * 4,
                (this.width / 3 - MARGIN * 4) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.signWidth = new GuiTextField(
                16,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 7 + MARGIN * 8,
                (this.width / 2 - MARGIN * 4) / 3,
                fontRenderer.FONT_HEIGHT
        );
        this.signHeight = new GuiTextField(
                17,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6,
                fontRenderer.FONT_HEIGHT * 7 + MARGIN * 8,
                (this.width / 2 - MARGIN * 4) / 3,
                fontRenderer.FONT_HEIGHT
        );
        this.signDepth = new GuiTextField(
                18,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6 * 2,
                fontRenderer.FONT_HEIGHT * 7 + MARGIN * 8,
                (this.width / 2 - MARGIN * 4) / 3,
                fontRenderer.FONT_HEIGHT
        );

        this.japaneseFont = new GuiTextField(
                21,
                fontRenderer,
                MARGIN,
                this.height / 2 + MARGIN + fontRenderer.FONT_HEIGHT,
                this.width / 2 - MARGIN * 2,
                fontRenderer.FONT_HEIGHT
        );

        this.englishFont = new GuiTextField(
                22,
                fontRenderer,
                MARGIN,
                this.height / 2 + MARGIN * 3 + fontRenderer.FONT_HEIGHT * 3,
                this.width / 2 - MARGIN * 2,
                fontRenderer.FONT_HEIGHT
        );

        this.modelSearch = new GuiTextField(
                51,
                fontRenderer,
                this.width / 2 + MARGIN,
                this.height / 2 + fontRenderer.FONT_HEIGHT + MARGIN,
                this.width / 2 - MARGIN * 2,
                fontRenderer.FONT_HEIGHT
        );

        if (this.tileEntity.isGenerated()) {
            this.japanese.setText(this.tileEntity.getInfo().japanese);
            this.english.setText(this.tileEntity.getInfo().english);
            this.backGroundColor.setText(String.format("%1$x", this.tileEntity.getInfo().color.getRGB()).substring(2));
            this.foreGroundColor.setText(String.format("%1$x", this.tileEntity.getInfo().textColor.getRGB()).substring(2));
            this.japaneseFont.setText(this.tileEntity.getInfo().japaneseFont);
            this.englishFont.setText(this.tileEntity.getInfo().englishFont);
            this.use114Sign.setIsChecked(true);
        }

        this.addButton(this.use114Sign);
        this.signWidth.setText(String.valueOf(Math.round(this.tileEntity.getWidth() * 1000f) / 1000f));
        this.signHeight.setText(String.valueOf(Math.round(this.tileEntity.getHeight() * 1000f) / 1000f));
        this.signDepth.setText(String.valueOf(Math.round(this.tileEntity.getDepth() * 1000f) / 1000f));

        // 地名板に使用可能なGTSScrollListを拵える

        this.textureScrollList = new GTSGuiScrollList(
                this.tileEntity,
                this.width / 2,
                this.height / 4,
                this.width / 2,
                this.height / 2 + fontRenderer.FONT_HEIGHT * 2 + MARGIN,
                choices
        );
        this.widgets.add(this.textureScrollList);

        this.textureScrollList.setSelectedChoice(this.cache.getName() + ":" + this.tileEntity.getConfig().getTextures().getBase());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 各ウィジェット描画
        this.modelSearch.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.114sign"), this.japanese.x, this.japanese.y - this.fontRenderer.FONT_HEIGHT * 2 - MARGIN * 2, 0xffffff);
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.japanese"), this.japanese.x, this.japanese.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.japanese.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.english"), this.english.x, this.english.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.english.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.color.fore"), this.foreGroundColor.x, this.foreGroundColor.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.foreGroundColor.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.color.back"), this.backGroundColor.x, this.backGroundColor.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.backGroundColor.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.width"), this.signWidth.x, this.signWidth.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.signWidth.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.height"), this.signHeight.x, this.signHeight.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.signHeight.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.depth"), this.signDepth.x, this.signDepth.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.signDepth.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.japanese.font"), this.japaneseFont.x, this.japaneseFont.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.japaneseFont.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.sign.english.font"), this.englishFont.x, this.englishFont.y - this.fontRenderer.FONT_HEIGHT - MARGIN, 0xffffff);
        this.englishFont.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.model_search"), this.width / 2 + MARGIN, this.height / 2 + MARGIN, 0xffffff);

        // 左上にテクスチャ描画
        ResourceLocation texture = tileEntity.getTexture();
        if (texture != null) Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GTSGui114Sign.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1024, 1024, Math.min(this.width / 2, this.height / 2), Math.min(this.width / 2, this.height / 2), 1024, 1024);

        // メッセージ描画
        this.drawString(fontRenderer, this.message, MARGIN, this.height / 2 + MARGIN * 6 + fontRenderer.FONT_HEIGHT * 5 + 20, 0xffff00);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.japanese.textboxKeyTyped(typedChar, keyCode);
        this.english.textboxKeyTyped(typedChar, keyCode);
        this.foreGroundColor.textboxKeyTyped(typedChar, keyCode);
        this.backGroundColor.textboxKeyTyped(typedChar, keyCode);
        this.signWidth.textboxKeyTyped(typedChar, keyCode);
        this.signHeight.textboxKeyTyped(typedChar, keyCode);
        this.signDepth.textboxKeyTyped(typedChar, keyCode);
        this.japaneseFont.textboxKeyTyped(typedChar, keyCode);
        this.englishFont.textboxKeyTyped(typedChar, keyCode);
        this.modelSearch.textboxKeyTyped(typedChar, keyCode);

        this.foreGroundColor.setTextColor(0xffffff);
        this.backGroundColor.setTextColor(0xffffff);
        this.signWidth.setTextColor(0xffffff);
        this.signHeight.setTextColor(0xffffff);
        this.signDepth.setTextColor(0xffffff);
        this.japaneseFont.setTextColor(0xffffff);
        this.englishFont.setTextColor(0xffffff);

        // モデルパックの絞り込み
        this.cache = this.getTextureChoices();
        this.textureScrollList.setChoices(this.choices);
        if (this.cache != null) {
            this.textureScrollList.setSelectedChoice(this.cache.getName() + ":" + this.tileEntity.getConfig().getTextures().getBase());
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.japanese.mouseClicked(mouseX, mouseY, mouseButton);
        this.english.mouseClicked(mouseX, mouseY, mouseButton);
        this.foreGroundColor.mouseClicked(mouseX, mouseY, mouseButton);
        this.backGroundColor.mouseClicked(mouseX, mouseY, mouseButton);
        this.signWidth.mouseClicked(mouseX, mouseY, mouseButton);
        this.signHeight.mouseClicked(mouseX, mouseY, mouseButton);
        this.signDepth.mouseClicked(mouseX, mouseY, mouseButton);
        this.japaneseFont.mouseClicked(mouseX, mouseY, mouseButton);
        this.englishFont.mouseClicked(mouseX, mouseY, mouseButton);
        this.modelSearch.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 1) {
            try {
                this.tileEntity.setWidth(Double.parseDouble(this.signWidth.getText()));
            } catch (NumberFormatException e) {
                // 赤文字にしてエラーを強調
                this.signWidth.setTextColor(0xff0000);
                this.message = I18n.format("gts.gui.sign.message.fail");
                return;
            }
            try {
                this.tileEntity.setHeight(Double.parseDouble(this.signHeight.getText()));
            } catch (NumberFormatException e) {
                // 赤文字にしてエラーを強調
                this.signHeight.setTextColor(0xff0000);
                this.message = I18n.format("gts.gui.sign.message.fail");
                return;
            }
            try {
                this.tileEntity.setDepth(Double.parseDouble(this.signDepth.getText()));
            } catch (NumberFormatException e) {
                // 赤文字にしてエラーを強調
                this.signDepth.setTextColor(0xff0000);
                this.message = I18n.format("gts.gui.sign.message.fail");
                return;
            }

            // 地名板の情報を適用して送信
            if (this.use114Sign.isChecked()) {
                GTSSignBase info = this.applyInfo();
                if (info == null) return;

                tileEntity.setTexture(GTSSignTextureManager.PLACE_HOLDER);
                this.tileEntity.setInfo(info);

            }
            else {
                // テクスチャを選択した場合
                // テクスチャを取得する
                this.tileEntity.setInfo(null);
                GTSConfig config = this.textureScrollList.getSelectedChoice();
                if (config == null) {
                    // しっかり選択できていなーい
                    this.message = I18n.format("gts.gui.sign.message.missingtexture");
                    return;
                }
                GTSConfig.GTSTexture texture = config.getTextures();
                if (texture == null) {
                    // ふつうあり得ないが、メモリ不整合の時を考える
                    this.message = I18n.format("gts.gui.sign.message.missingtexture");
                    return;
                }
                String textureName = texture.getBase();

                // テクスチャを取得し、なければバインドする
                for (GTSPack pack: GTS.LOADER.getPacks()) {
                    if (pack.dummy()) continue;
                    if (!pack.getTextures().containsKey(textureName)) continue;
                    ResourceLocation rs = pack.getOrCreateBindTexture(textureName);
                    if (rs == null) {
                        this.message = I18n.format("gts.gui.sign.message.missingtexture");
                        return;
                    }
                    // バインド
                    this.tileEntity.setTexture(rs);
                    this.tileEntity.setConfig(config);
                    this.tileEntity.setPack(pack);
                    break;
                }
            }
            this.message = I18n.format("gts.gui.sign.message.apply");
            GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.tileEntity.writeToNBT(new NBTTagCompound()), this.tileEntity.getPos(), GTSTileEntityTrafficSign.class));
            tileEntity.markDirty();
            tileEntity.getWorld().notifyBlockUpdate(
                    tileEntity.getPos(),
                    tileEntity.getWorld().getBlockState(tileEntity.getPos()),
                    tileEntity.getWorld().getBlockState(tileEntity.getPos()),
                    3);
        }
        else if (button.id == 2) {
            // フォント一覧をログに出す
            ArrayList<String> fonts = GTSSignTextureManager.getAvailableFonts();
            GTS.LOGGER.info(I18n.format("gts.gui.sign.font.available"));
            for (String font: fonts) {
                GTS.LOGGER.info(font);
            }
            this.message = I18n.format("gts.gui.sign.message.fonts");
        }

        else if (button.id == 3) {
            // 114-A地名板のチェックボックスのトグルがされたとき、それがTrueへの設定ならばセレクトボックスの選択を解除する
            // ついでにTileEntityのテクスチャを切り替える
            if (this.use114Sign.isChecked()) {
                this.textureScrollList.resetChoice();
                this.tileEntity.setTexture(GTSSignTextureManager.PLACE_HOLDER);
                this.tileEntity.setInfo(new GTS114Sign());
            }
        }
    }

    /**
     * GUIで行った変更を全て反映させるメソッド。
     * 必ずフィールドを追加した場合はここもオーバーライドすること。
     *
     * @return エラーチェックに引っ掛かったらnull
     */
    public T applyInfo() {
        T info = null;
        try {
            // 苦肉の策で無理やりインスタンスを作成する
            info = this.clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            int c1 = Integer.parseInt(this.foreGroundColor.getText().toUpperCase(), 16);
            info.textColor = new Color(c1);
        } catch (NumberFormatException e) {
            // 赤文字にしてエラーを強調
            this.foreGroundColor.setTextColor(0xff0000);
            this.message = I18n.format("gts.gui.sign.message.fail");
            return null;
        }
        try {
            int c2 = Integer.parseInt(this.backGroundColor.getText().toUpperCase(), 16);
            info.color = new Color(c2);
        } catch (NumberFormatException e) {
            // 赤文字にしてエラーを強調
            this.backGroundColor.setTextColor(0xff0000);
            this.message = I18n.format("gts.gui.sign.message.fail");
            return null;
        }

        // フォントはちゃんとした名前かどうかチェック
        ArrayList<String> fonts = GTSSignTextureManager.getAvailableFonts();
        if (!fonts.contains(japaneseFont.getText())) {
            this.japaneseFont.setTextColor(0xff0000);
            return null;
        }
        if (!fonts.contains(englishFont.getText())) {
            this.englishFont.setTextColor(0xff0000);
            return null;
        }

        info.japanese = this.japanese.getText();
        info.english = this.english.getText();
        info.japaneseFont = this.japaneseFont.getText();
        info.englishFont = this.englishFont.getText();

        info.aspect = this.tileEntity.getWidth() / this.tileEntity.getHeight();

        return info;
    }
}
