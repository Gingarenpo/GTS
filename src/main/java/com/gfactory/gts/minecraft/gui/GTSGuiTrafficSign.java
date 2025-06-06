package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GTSGuiTrafficSign extends GTSGui<GTSTileEntityTrafficSign> {

    /**
     * 日本語部分
     */
    private GuiTextField japanese;

    /**
     * 英語部分
     */
    private GuiTextField english;

    /**
     * 文字色・枠色
     */
    private GuiTextField foreGroundColor;

    /**
     * 背景色
     */
    private GuiTextField backGroundColor;

    /**
     * 幅固定？
     */
    private GuiCheckBox fixed;

    /**
     * 設置する地名板の幅
     */
    private GuiTextField signWidth;

    /**
     * 設置する地名板の高さ
     */
    private GuiTextField signHeight;

    /**
     * 設置する地名板の奥行
     */
    private GuiTextField signDepth;

    /**
     * 日本語フォント
     */
    private GuiTextField japaneseFont;

    /**
     * 英語フォント
     */
    private GuiTextField englishFont;

    /**
     * メッセージ（なんかいろいろ書くところ）
     */
    private String message;

    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficSign(GTSTileEntityTrafficSign tileEntity) {
        super(tileEntity);
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

        this.addButton(new GuiCheckBox(
                14,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 7 + MARGIN * 8,
                I18n.format("gts.gui.sign.fixed"),
                tileEntity.is114Sign() && tileEntity.getInfo().widthFix
        ));

        this.japanese = new GuiTextField(
                11,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 2 + MARGIN * 3,
                (this.width / 2 - MARGIN * 3) / 2,
                fontRenderer.FONT_HEIGHT
        );
        this.english = new GuiTextField(
                12,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 4,
                fontRenderer.FONT_HEIGHT * 2 + MARGIN * 3,
                (this.width / 2 - MARGIN * 3) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.foreGroundColor = new GuiTextField(
                13,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 4 + MARGIN * 5,
                (this.width / 3 - MARGIN * 4) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.backGroundColor = new GuiTextField(
                15,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6,
                fontRenderer.FONT_HEIGHT * 4 + MARGIN * 5,
                (this.width / 3 - MARGIN * 4) / 2,
                fontRenderer.FONT_HEIGHT
        );

        this.signWidth = new GuiTextField(
                16,
                fontRenderer,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 6 + MARGIN * 7,
                (this.width / 2 - MARGIN * 4) / 3,
                fontRenderer.FONT_HEIGHT
        );
        this.signHeight = new GuiTextField(
                17,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6,
                fontRenderer.FONT_HEIGHT * 6 + MARGIN * 7,
                (this.width / 2 - MARGIN * 4) / 3,
                fontRenderer.FONT_HEIGHT
        );
        this.signDepth = new GuiTextField(
                18,
                fontRenderer,
                this.width / 2 + MARGIN + this.width / 6 * 2,
                fontRenderer.FONT_HEIGHT * 6 + MARGIN * 7,
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

        if (this.tileEntity.is114Sign()) {
            this.japanese.setText(this.tileEntity.getInfo().japanese);
            this.english.setText(this.tileEntity.getInfo().english);
            this.backGroundColor.setText(String.format("%1$x", this.tileEntity.getInfo().color.getRGB()).substring(2));
            this.foreGroundColor.setText(String.format("%1$x", this.tileEntity.getInfo().textColor.getRGB()).substring(2));
            this.signWidth.setText(String.valueOf(Math.round(this.tileEntity.getWidth() * 1000f) / 1000f));
            this.signHeight.setText(String.valueOf(Math.round(this.tileEntity.getHeight() * 1000f) / 1000f));
            this.signDepth.setText(String.valueOf(Math.round(this.tileEntity.getDepth() * 1000f) / 1000f));
            this.japaneseFont.setText(this.tileEntity.getInfo().japaneseFont);
            this.englishFont.setText(this.tileEntity.getInfo().englishFont);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 各ウィジェット描画
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

        // 左上にテクスチャ描画
        ResourceLocation texture = tileEntity.getTexture();
        if (texture != null) Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GTSGuiTrafficSign.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1024, 1024, Math.min(this.width / 2, this.height / 2), Math.min(this.width / 2, this.height / 2), 1024, 1024);

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

        this.foreGroundColor.setTextColor(0xffffff);
        this.backGroundColor.setTextColor(0xffffff);
        this.signWidth.setTextColor(0xffffff);
        this.signHeight.setTextColor(0xffffff);
        this.signDepth.setTextColor(0xffffff);
        this.japaneseFont.setTextColor(0xffffff);
        this.englishFont.setTextColor(0xffffff);

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

            // 地名板の情報を適用
            if (this.tileEntity.is114Sign()) {
                GTSSignTextureManager.GTS114Sign info = this.tileEntity.getInfo();
                try {
                    int c1 = Integer.parseInt(this.foreGroundColor.getText().toUpperCase(), 16);
                    info.textColor = new Color(c1);
                } catch (NumberFormatException e) {
                    // 赤文字にしてエラーを強調
                    this.foreGroundColor.setTextColor(0xff0000);
                    this.message = I18n.format("gts.gui.sign.message.fail");
                    return;
                }
                try {
                    int c2 = Integer.parseInt(this.backGroundColor.getText().toUpperCase(), 16);
                    info.color = new Color(c2);
                } catch (NumberFormatException e) {
                    // 赤文字にしてエラーを強調
                    this.backGroundColor.setTextColor(0xff0000);
                    this.message = I18n.format("gts.gui.sign.message.fail");
                    return;
                }

                // フォントはちゃんとした名前かどうかチェック
                ArrayList<String> fonts = GTSSignTextureManager.getAvailableFonts();
                if (!fonts.contains(japaneseFont.getText())) {
                    this.japaneseFont.setTextColor(0xff0000);
                    return;
                }
                if (!fonts.contains(englishFont.getText())) {
                    this.englishFont.setTextColor(0xff0000);
                    return;
                }

                info.japanese = this.japanese.getText();
                info.english = this.english.getText();
                info.japaneseFont = this.japaneseFont.getText();
                info.englishFont = this.englishFont.getText();

                // チェックボックスを取得して、その値を見る
                for (GuiButton b: this.buttonList) {
                    if (b.id == 14) info.widthFix = ((GuiCheckBox) b).isChecked();
                }

                info.aspect = this.tileEntity.getWidth() / this.tileEntity.getHeight();

                tileEntity.setTexture(null);

                this.message = I18n.format("gts.gui.sign.message.apply");

                GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.tileEntity.writeToNBT(new NBTTagCompound()), this.tileEntity.getPos(), GTSTileEntityTrafficSign.class));
                tileEntity.markDirty();
                tileEntity.getWorld().notifyBlockUpdate(
                        tileEntity.getPos(),
                        tileEntity.getWorld().getBlockState(tileEntity.getPos()),
                        tileEntity.getWorld().getBlockState(tileEntity.getPos()),
                        3);

            }
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
    }
}
