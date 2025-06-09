package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.widget.GTSGuiModelView;
import com.gfactory.gts.minecraft.gui.widget.GTSGuiScrollList;
import com.gfactory.gts.minecraft.network.packet.GTSPacketTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityDummy;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.TreeMap;

public abstract class GTSGuiModelChoose<T extends GTSTileEntity> extends GTSGui {
    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiModelChoose(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public void initGui() {
        super.initGui();
        TreeMap<String, ? extends GTSConfig> elements = this.getModelElements();
        if (elements.isEmpty()) {
            elements.put(I18n.format("gts.gui.model.none"), null);
        }
        this.widgets.add(new GTSGuiModelView(this.tileEntity, this.width / 4, this.width / 4, this.width / 8, MARGIN));
        this.widgets.add(new GTSGuiScrollList(
                this.tileEntity,
                this.width / 2,
                this.height - this.width / 4 - MARGIN * 4 - 20,
                MARGIN,
                this.width / 4 + MARGIN * 3 + 20,
                elements
                ));

        // モデルを変更するボタンを追加
        GuiButton b = new GuiButton(10001, this.width / 8, this.width / 4 + MARGIN * 2, I18n.format("gts.gui.model_choose"));
        b.width = this.width / 4;
        this.addButton(b);
    }

    /**
     * このGUIで選択できるモデルとして、その一覧を文字列にして返す。
     * @return
     */
    public abstract TreeMap<String, ? extends GTSConfig> getModelElements();

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 10001) {
            // Choose Modelが押された
            GTSGuiScrollList w = (GTSGuiScrollList) this.widgets.get(1);
            GTSConfig c = w.getSelectedChoice();
            if (c == null) return;
            for (GTSPack p: GTS.LOADER.getPacks()) {
                if (p.getConfigs().containsValue(c)) {
                    this.tileEntity.setPack(p);
                    this.tileEntity.setConfig(c);
                    if (tileEntity instanceof GTSTileEntityDummy) return; // ダミーの場合は送信を行わない
                    this.tileEntity.markDirty();
                    GTS.NETWORK.sendToServer(new GTSPacketTileEntity(this.tileEntity.getUpdateTag(), this.tileEntity.getPos(), GTSTileEntity.class));
                    this.tileEntity.getWorld().notifyBlockUpdate(
                            this.tileEntity.getPos(),
                            this.tileEntity.getWorld().getBlockState(this.tileEntity.getPos()),
                            this.tileEntity.getWorld().getBlockState(this.tileEntity.getPos()),
                            3);
                    return;
                }
            }
            throw new RuntimeException("あ！");
        }
    }
}
