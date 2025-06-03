package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;

/**
 * 地名板、標示板などを追加するためのブロック
 */
public class GTSBlockTrafficSign extends GTSBlock<GTSTileEntityTrafficSign> {

    public GTSBlockTrafficSign() {
        super(GTSTileEntityTrafficSign.class);
        this.setRegistryName("traffic_sign");
        this.setUnlocalizedName("traffic_sign");
    }

}
