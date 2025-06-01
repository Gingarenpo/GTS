package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.common.controller.GTSCycle;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.pack.config.GTSConfig;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * 交通信号制御機のサイクル、フェーズ、チャンネルなどを設定するGUI。
 * Minecraft内で動作するようにしているが、GuiScreenは絶対座標指定による気持ち悪い
 * ハードコーディングを強いられるので読みづらくなっているかもしれない。
 */
public class GTSGuiTrafficController extends GTSGuiModelChoose<GTSTileEntityTrafficController> {

    private String errorMessage = "";

    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficController(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public TreeMap<String, ? extends GTSConfig> getModelElements() {
        return new TreeMap<>();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 1) {
            // サイクルをロードする
            // 流石にMinecraftのGUIだけでは限界があるのでSwingを使う
            JFileChooser fc = new JFileChooser(Minecraft.getMinecraft().mcDataDir);
            fc.addChoosableFileFilter(new FileNameExtensionFilter(I18n.format("gts.gui.controller.extension"), "gts"));
            int result = fc.showDialog(null, I18n.format("gts.gui.controller.load")); // Minecraftフリーズするけど仕方ない
            if (result != JFileChooser.APPROVE_OPTION) return; // キャンセルされた場合などは実行しない

            // サイクルのロードを行う
            File file = fc.getSelectedFile();
            if (file == null) return;
            try (FileReader fr = new FileReader(file)) {
                //
                GTSCycle[] cycle = GTS.GSON.fromJson(fr, GTSCycle[].class); // サイクル情報を読み込みパース
                GTSTileEntityTrafficController te = (GTSTileEntityTrafficController) tileEntity;
                te.setCycles(new ArrayList<>(Arrays.asList(cycle)));
                te.markDirty();
                GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(te.writeToNBT(new NBTTagCompound()), te.getPos(), GTSTileEntityTrafficController.class));
                this.errorMessage = I18n.format("gts.gui.controller.load.done");
            } catch (JsonParseException e) {
                this.errorMessage = I18n.format("gts.gui.controller.load.error", e.getLocalizedMessage());
            }
        }
        else if (button.id == 2) {
            // セーブする
            // 流石にMinecraftのGUIだけでは限界があるのでSwingを使う
            JFileChooser fc = new JFileChooser(Minecraft.getMinecraft().mcDataDir);
            fc.addChoosableFileFilter(new FileNameExtensionFilter(I18n.format("gts.gui.controller.extension"), "gts"));
            int result = fc.showDialog(null, I18n.format("gts.gui.controller.save")); // Minecraftフリーズするけど仕方ない
            if (result != JFileChooser.APPROVE_OPTION) return; // キャンセルされた場合などは実行しない

            // サイクルのロードを行う
            File file = fc.getSelectedFile();
            if (file == null) return;
            try (FileWriter fw = new FileWriter(file)) {

                GTSTileEntityTrafficController te = (GTSTileEntityTrafficController) tileEntity;
                String data = GTS.GSON.toJson(te.getCycles().toArray(), GTSCycle[].class); // サイクル情報を読み込みパース
                fw.write(data);

                this.errorMessage = I18n.format("gts.gui.controller.save.done");
            } catch (JsonParseException | IOException e) {
                this.errorMessage = I18n.format("gts.gui.controller.save.error", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        // 制御機情報出力ボタンと入力ボタンを作成
        this.addButton(new GuiButton(
            1,
            this.width / 2 + MARGIN,
            this.fontRenderer.FONT_HEIGHT + MARGIN,
            this.width / 2 - MARGIN * 2,
            20,
            I18n.format("gts.gui.controller.load")
        ));
        this.addButton(new GuiButton(
                2,
                this.width / 2 + MARGIN,
                this.fontRenderer.FONT_HEIGHT + MARGIN * 2 + 20,
                this.width / 2 - MARGIN * 2,
                20,
                I18n.format("gts.gui.controller.save")
        ));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        // エラーメッセージ表示部分
        this.drawString(fontRenderer, this.errorMessage,this.width / 2 + MARGIN, this.fontRenderer.FONT_HEIGHT + MARGIN * 3 + 40, 0xffff00);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
