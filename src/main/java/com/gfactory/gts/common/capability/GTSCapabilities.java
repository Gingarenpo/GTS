package com.gfactory.gts.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * GTSが何かしらの情報を保持させることができるようにアタッチするためのキャパを登録するところ
 */
public class GTSCapabilities {

    /**
     * 交通信号制御機とか何かしらのアタッチ関連、クリックしてもう片方をクリック…みたいな
     */
    @CapabilityInject(IGTSSelection.class)
    public static Capability<IGTSSelection> SELECTION_CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(
                IGTSSelection.class,
                new Capability.IStorage<IGTSSelection>() {
                    @Override
                    public NBTBase writeNBT(Capability<IGTSSelection> capability, IGTSSelection instance, EnumFacing side) {
                        NBTTagCompound tag = new NBTTagCompound();
                        if (instance.getSelectedTileEntity() != null) {
                            tag.setLong("pos", instance.getSelectedTileEntity().toLong());
                        }
                        return tag;
                    }

                    @Override
                    public void readNBT(Capability<IGTSSelection> capability, IGTSSelection instance, EnumFacing side, NBTBase nbt) {
                        if (nbt instanceof NBTTagCompound) {
                            NBTTagCompound tag = (NBTTagCompound) nbt;
                            if (tag.hasKey("pos")) {
                                instance.setSelectedTileEntity(BlockPos.fromLong(tag.getLong("pos")));
                            }
                        }
                    }
                },
                GTSSelection::new
        );
    }
}
