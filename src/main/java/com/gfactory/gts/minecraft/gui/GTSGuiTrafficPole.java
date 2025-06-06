package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficPoleConfig;

import java.util.Map;
import java.util.TreeMap;

public class GTSGuiTrafficPole extends GTSGuiModelChoose<GTSTileEntityTrafficPole> {
    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficPole(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public TreeMap<String, ? extends GTSConfig> getModelElements() {
        TreeMap<String, GTSTrafficPoleConfig> elements = new TreeMap<>();
        for (GTSPack p: GTS.LOADER.getPacks()) {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> e: p.getConfigs().entrySet()) {
                if (!(e.getValue() instanceof GTSTrafficPoleConfig)) continue;
                GTSTrafficPoleConfig c = (GTSTrafficPoleConfig) e.getValue();
                elements.put(p.getName() + ": " + c.getId(), c);
            }
        }
        return elements;
    }
}
