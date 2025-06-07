package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficButtonConfig;

import java.util.Map;
import java.util.TreeMap;

public class GTSGuiTrafficButton extends GTSGuiModelChoose<GTSTileEntityTrafficButton> {
    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficButton(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public TreeMap<String, GTSTrafficButtonConfig> getModelElements() {
        TreeMap<String, GTSTrafficButtonConfig> elements = new TreeMap<>();
        for (GTSPack p: GTS.LOADER.getPacks()) {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> e: p.getConfigs().entrySet()) {
                if (!(e.getValue() instanceof GTSTrafficButtonConfig)) continue;
                GTSTrafficButtonConfig c = (GTSTrafficButtonConfig) e.getValue();
                elements.put(p.getName() + ": " + c.getId(), c);
            }
        }
        return elements;
    }
}
