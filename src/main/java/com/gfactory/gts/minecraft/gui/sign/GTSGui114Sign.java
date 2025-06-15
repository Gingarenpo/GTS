package com.gfactory.gts.minecraft.gui.sign;

import com.gfactory.gts.common.GTSSignTextureManager;
import com.gfactory.gts.common.sign.GTS114Sign;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficSign;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GTSGui114Sign extends GTSGuiTrafficSign<GTS114Sign> {

    /**
     * 幅固定？
     */
    private GuiCheckBox fixed;


    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGui114Sign(GTSTileEntityTrafficSign tileEntity) {
        super(tileEntity, GTS114Sign.class);
    }


    @Override
    public void initGui() {
        super.initGui();
        this.addButton(new GuiCheckBox(
                14,
                this.width / 2 + MARGIN,
                fontRenderer.FONT_HEIGHT * 7 + MARGIN * 8,
                I18n.format("gts.gui.sign.fixed"),
                tileEntity.isGenerated() && tileEntity.getInfo() instanceof GTS114Sign && ((GTS114Sign) tileEntity.getInfo()).widthFix
        ));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
            if (this.tileEntity.isGenerated()) {
                GTS114Sign info = new GTS114Sign();
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

                this.tileEntity.setInfo(info);
                tileEntity.setTexture(GTSSignTextureManager.PLACE_HOLDER);

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

    @Override
    public GTS114Sign applyInfo() {
        GTS114Sign info = super.applyInfo();
        if (info == null) return null;
        // チェックボックスを取得して、その値を見る
        for (GuiButton b: this.buttonList) {
            if (b.id == 14) info.widthFix = ((GuiCheckBox) b).isChecked();
        }
        return info;
    }
}
