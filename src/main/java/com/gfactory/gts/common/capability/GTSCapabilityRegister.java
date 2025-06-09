package com.gfactory.gts.common.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class GTSCapabilityRegister {

    /**
     * プレイヤーにキャパビリティをアタッチするためのイベント
     *
     * @param event イベント
     */
    @SubscribeEvent
    public static void attachCapabilitiesToPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            // プレイヤーだった場合
            event.addCapability(new ResourceLocation("gts", "selection"),
                    // ここでキャパビリティのシリアライズ可能なデータを用意する
                    new ICapabilitySerializable<NBTTagCompound>() {

                        private final GTSSelection instance = new GTSSelection();

                        @Override
                        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                            return capability == GTSCapabilities.SELECTION_CAP;
                        }

                        @Nullable
                        @Override
                        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                            return capability == GTSCapabilities.SELECTION_CAP
                                    ? GTSCapabilities.SELECTION_CAP.cast(instance)
                                    : null;
                        }

                        @Override
                        public NBTTagCompound serializeNBT() {
                            return (NBTTagCompound) GTSCapabilities.SELECTION_CAP.writeNBT(instance, null);
                        }

                        @Override
                        public void deserializeNBT(NBTTagCompound nbt) {
                            GTSCapabilities.SELECTION_CAP.readNBT(instance, null, nbt);
                        }
                    });
        }
    }
}
