package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.network.packet.GTSPacketItemNBT;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficArm;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficArmConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class GTSGuiTrafficArm extends GTSGuiModelChoose<GTSTileEntityTrafficArm> {

    /**
     * ここはアイテムが必要とされる
     */
    private ItemStack item;

    /**
     * アイテムのスロット番号
     */
    private int slot;

    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficArm(GTSTileEntity tileEntity, ItemStack item, int slot) {
        super(tileEntity);
        this.item = item;
        this.slot = slot;
    }

    @Override
    public TreeMap<String, ? extends GTSConfig> getModelElements() {
        TreeMap<String, GTSTrafficArmConfig> elements = new TreeMap<>();
        for (GTSPack p: GTS.LOADER.getPacks()) {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> e: p.getConfigs().entrySet()) {
                if (!(e.getValue() instanceof GTSTrafficArmConfig)) continue;
                GTSTrafficArmConfig c = (GTSTrafficArmConfig) e.getValue();
                elements.put(p.getName() + ": " + c.getId(), c);
            }
        }
        return elements;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        // ItemStackに対して書き込みを入れる
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) compound = new NBTTagCompound();
        compound = this.tileEntity.writeToNBT(compound);
        this.item.setTagCompound(compound);
        GTS.NETWORK.sendToServer(new GTSPacketItemNBT(this.slot, compound));
    }
}
