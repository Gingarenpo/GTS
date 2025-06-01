package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficLightConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 交通信号機のモデル選択や、チャンネルの設定を行う
 */
public class GTSGuiTrafficLight extends GTSGuiModelChoose<GTSTileEntityTrafficLight> {

    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficLight(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    /**
     * チャンネルの名前を保持するフィールド
     */
    private GuiTextField channelName;

    @Override
    public void initGui() {
        super.initGui();

        // チャンネルの設定を行うテキストボックスを作成
        this.channelName = new GuiTextField(0, fontRenderer, this.width / 2 + MARGIN, fontRenderer.FONT_HEIGHT * 2 + MARGIN * 2, this.width / 2 - MARGIN * 2, fontRenderer.FONT_HEIGHT);
        this.channelName.setMaxStringLength(32);
        this.channelName.setFocused(true);
        this.channelName.setText(((GTSTileEntityTrafficLight) tileEntity).getChannel());

        // その下にボタンを作成
        this.addButton(new GuiButton(1, this.width / 2 + MARGIN, fontRenderer.FONT_HEIGHT * 3 + MARGIN * 3, I18n.format("gts.gui.channel.apply")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 右側パネル2行目から、ラベルを配置
        fontRenderer.drawString(I18n.format("gts.gui.channel.name"), this.width / 2 + MARGIN, fontRenderer.FONT_HEIGHT + MARGIN, 0xFFFFFF);
        this.channelName.drawTextBox();
    }

    @Override
    public TreeMap<String, GTSTrafficLightConfig> getModelElements() {
        TreeMap<String, GTSTrafficLightConfig> elements = new TreeMap<>();
        for (GTSPack p: GTS.LOADER.getPacks()) {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> e: p.getConfigs().entrySet()) {
                if (!(e.getValue() instanceof GTSTrafficLightConfig)) continue;
                GTSTrafficLightConfig c = (GTSTrafficLightConfig) e.getValue();
                elements.put(p.getName() + ": " + c.getId(), c);
            }
        }
        return elements;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.channelName.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.channelName.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.channelName.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 1) {
            // チャンネル名を更新する
            GTSTileEntityTrafficLight te = (GTSTileEntityTrafficLight) tileEntity;
            te.setChannel(this.channelName.getText());
            GTS.NETWORK.sendToServer(new GTSPacketTileEntity<>(te.writeToNBT(new NBTTagCompound()), te.getPos(), GTSTileEntityTrafficLight.class));
            te.markDirty();
            te.getWorld().notifyBlockUpdate(
                    te.getPos(),
                    te.getWorld().getBlockState(te.getPos()),
                    te.getWorld().getBlockState(te.getPos()),
                    3);
        }
    }
}
