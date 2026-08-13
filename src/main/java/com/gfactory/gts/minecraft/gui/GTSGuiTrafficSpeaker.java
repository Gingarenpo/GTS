package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSpeaker;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSConfig;
import com.gfactory.gts.pack.config.GTSTrafficSpeakerConfig;

import java.util.Map;
import java.util.TreeMap;

public class GTSGuiTrafficSpeaker extends GTSGuiModelChoose<GTSTileEntityTrafficSpeaker> {
    /**
     * 必ずTileEntityを渡す必要がある
     *
     * @param tileEntity このGUIで使用するTileEntity
     */
    public GTSGuiTrafficSpeaker(GTSTileEntity tileEntity) {
        super(tileEntity);
    }

    @Override
    public TreeMap<String, ? extends GTSConfig> getModelElements() {
        TreeMap<String, GTSTrafficSpeakerConfig> elements = new TreeMap<>();
        for (GTSPack p: GTS.LOADER.getPacks()) {
            for (Map.Entry<String, GTSConfig<GTSConfig.GTSTexture>> e: p.getConfigs().entrySet()) {
                if (!(e.getValue() instanceof GTSTrafficSpeakerConfig)) continue;
                GTSTrafficSpeakerConfig c = (GTSTrafficSpeakerConfig) e.getValue();
                elements.put(p.getName() + ": " + c.getId(), c);
            }
        }
        return elements;
    }
}
