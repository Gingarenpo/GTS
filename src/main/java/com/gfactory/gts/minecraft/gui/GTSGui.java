package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.widget.GTSWidget;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityDummy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * GTS内部で使用するGUIの表示を行う。
 * 基本前処理などはこちらが行う。
 * なお、各TileEntity即したGUI描画を行うため、Tには使用するTileEntityを挿入する必要がある。
 */
public abstract class GTSGui<T extends GTSTileEntity> extends GuiScreen {

    /**
     * このGUIが保持するTileEntity
     */
    protected T tileEntity;

    /**
     * 描画すべきWidget
     */
    protected ArrayList<GTSWidget> widgets = new ArrayList<>();

    /**
     * マージン
     */
    public static final int MARGIN = 2;

    /**
     * 座標X相対ずれ
     */
    private GuiTextField posX;

    /**
     * 座標Y相対ずれ
     */
    private GuiTextField posY;

    /**
     * 座標Z相対ずれ
     */
    private GuiTextField posZ;

    /**
     * 必ずTileEntityを渡す必要がある
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGui(T tileEntity) {
        this.tileEntity = tileEntity;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 20001) {
            if (this.posX.getText().isEmpty()) this.posX.setText("0");
            if (this.posY.getText().isEmpty()) this.posY.setText("0");
            if (this.posZ.getText().isEmpty()) this.posZ.setText("0");
            // 座標を検出
            try {
                double posX = Math.round(Double.parseDouble(this.posX.getText()) * 1000f) / 1000f;
                double posY = Math.round(Double.parseDouble(this.posY.getText()) * 1000f) / 1000f;
                double posZ = Math.round(Double.parseDouble(this.posZ.getText()) * 1000f) / 1000f;

                // TileEntity変更
                this.tileEntity.setPosX(posX);
                this.tileEntity.setPosY(posY);
                this.tileEntity.setPosZ(posZ);
                if (tileEntity instanceof GTSTileEntityDummy) return; // ダミーの場合は送信を行わない
                this.tileEntity.markDirty();
                GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(this.tileEntity.writeToNBT(new NBTTagCompound()), this.tileEntity.getPos(), GTSTileEntity.class));
                this.tileEntity.getWorld().notifyBlockUpdate(
                        this.tileEntity.getPos(),
                        this.tileEntity.getWorld().getBlockState(this.tileEntity.getPos()),
                        this.tileEntity.getWorld().getBlockState(this.tileEntity.getPos()),
                        3);
            } catch (NumberFormatException e) {
                // 浮動小数点として不適切
                this.posX.setTextColor(0xff0000);
                this.posY.setTextColor(0xff0000);
                this.posZ.setTextColor(0xff0000);

            }

        }
    }

    /**
     * GUI初期表示時、リサイズ時、ボタン解除時？に呼び出される。
     * 必要な初期処理を行う。
     */
    @Override
    public void initGui() {
        // 描画位置を決められるようにする入力欄
        this.addButton(new GuiButton(20001, this.width / 2 + MARGIN, this.height - MARGIN - 20, this.width / 2 - MARGIN * 2, 20, I18n.format("gts.gui.position.apply")));
        this.posX = new GuiTextField(20002, fontRenderer, this.width / 2 + MARGIN * 2, this.height - MARGIN * 2 - 20 - fontRenderer.FONT_HEIGHT, this.width / 6 - MARGIN * 5, this.fontRenderer.FONT_HEIGHT);
        this.posY = new GuiTextField(20003, fontRenderer, this.width / 2 + MARGIN * 3 + this.width / 6, this.height - MARGIN * 2 - 20 - fontRenderer.FONT_HEIGHT, this.width / 6 - MARGIN * 5, this.fontRenderer.FONT_HEIGHT);
        this.posZ = new GuiTextField(20004, fontRenderer, this.width / 2 + MARGIN * 4 + this.width / 3, this.height - MARGIN * 2 - 20 - fontRenderer.FONT_HEIGHT, this.width / 6 - MARGIN * 5, this.fontRenderer.FONT_HEIGHT);
        this.posX.setText(String.valueOf(Math.round(tileEntity.getPosX() * 1000f) / 1000f));
        this.posY.setText(String.valueOf(Math.round(tileEntity.getPosY() * 1000f) / 1000f));
        this.posZ.setText(String.valueOf(Math.round(tileEntity.getPosZ() * 1000f) / 1000f));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 黒半透明背景
        this.drawDefaultBackground();

        // ボタンやラベルを描画
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.posX.drawTextBox();
        this.posY.drawTextBox();
        this.posZ.drawTextBox();
        this.drawString(fontRenderer, I18n.format("gts.gui.position.x"), this.width / 2 + MARGIN * 3, this.posX.y - fontRenderer.FONT_HEIGHT, 0xFFFFFF);
        this.drawString(fontRenderer, I18n.format("gts.gui.position.y"), this.width / 2 + MARGIN * 3 + this.width / 6, this.posY.y - fontRenderer.FONT_HEIGHT, 0xFFFFFF);
        this.drawString(fontRenderer, I18n.format("gts.gui.position.z"), this.width / 2 + MARGIN * 3 + this.width / 3, this.posZ.y - fontRenderer.FONT_HEIGHT, 0xFFFFFF);


        // GUIタイトル
        this.drawCenteredString(fontRenderer, tileEntity.getName(), this.width / 2, GTSGui.MARGIN, 0xFFFFFF);

        // ウィジェットを描画
        for (GTSWidget w: this.widgets) {
            w.draw();
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.posX.setTextColor(0xffffff);
        this.posY.setTextColor(0xffffff);
        this.posZ.setTextColor(0xffffff);
        this.posX.textboxKeyTyped(typedChar, keyCode);
        this.posY.textboxKeyTyped(typedChar, keyCode);
        this.posZ.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        // マウスイベントを取得
        ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
        int scaledWidth = scaled.getScaledWidth();
        int scaledHeight = scaled.getScaledHeight();
        int realWidth = Minecraft.getMinecraft().displayWidth;
        int realHeight = Minecraft.getMinecraft().displayHeight;
        int dWheel = Mouse.getDWheel();

        int x = (int) (Mouse.getX() * ((float)scaledWidth / realWidth));
        int y = (int) (scaledHeight - (Mouse.getY() * ((float) scaledHeight / realHeight))); // なんとY座標原点下！！！
        int mouseButton = Mouse.getEventButton();
        for (GTSWidget w: this.widgets) {
            w.handleMouseInput(x, y, dWheel, mouseButton);
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (GTSWidget w: this.widgets) {
            w.mouseClicked(mouseX, mouseY, mouseButton);
        }
        this.posX.mouseClicked(mouseX, mouseY, mouseButton);
        this.posY.mouseClicked(mouseX, mouseY, mouseButton);
        this.posZ.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
