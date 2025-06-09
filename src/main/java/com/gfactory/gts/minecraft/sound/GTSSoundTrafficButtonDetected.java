package com.gfactory.gts.minecraft.sound;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import net.minecraft.util.ResourceLocation;

/**
 * ボタンが押されたときに鳴るサウンドイベント
 */
public class GTSSoundTrafficButtonDetected extends GTSSoundEvent<GTSTileEntityTrafficButton> {

    public GTSSoundTrafficButtonDetected(GTSTileEntityTrafficButton te) {
        super(te);
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return new ResourceLocation("gts_" + te.getPack().getName(), te.getConfig().getAudios().getDetected());
    }
}
