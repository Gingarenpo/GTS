package com.gfactory.gts.minecraft.sound;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import net.minecraft.util.ResourceLocation;

/**
 * 常時鳴るサウンド
 */
public class GTSSoundTrafficButtonBase extends GTSSoundEvent<GTSTileEntityTrafficButton> {
    public GTSSoundTrafficButtonBase(GTSTileEntityTrafficButton te) {
        super(te);
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return new ResourceLocation("gts_" + te.getPack().getName(), te.getConfig().getAudios().getBase());
    }
}
