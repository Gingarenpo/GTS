package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.common.capability.IGTSSelection;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.GTSGuiHandler;
import com.gfactory.gts.minecraft.item.GTSItems;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSpeaker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class GTSBlockTrafficSpeaker extends GTSBlock<GTSTileEntityTrafficSpeaker> {
    public GTSBlockTrafficSpeaker() {
        super(GTSTileEntityTrafficSpeaker.class);
        this.setRegistryName("traffic_speaker");
        this.setUnlocalizedName("traffic_speaker");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!(super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))) return false;
        if (hand != EnumHand.MAIN_HAND) return false; // 左手の場合は無視

        // 持っているものを取得
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (itemStack.isItemEqual(new ItemStack(GTSItems.ATTACHMENT))) {
            if (!worldIn.isRemote) {
                // アタッチメントを持っている場合、アタッチモードに入る
                IGTSSelection selection = playerIn.getCapability(GTSCapabilities.SELECTION_CAP, null);
                if (selection == null) return false;
                BlockPos selectedPos = selection.getSelectedTileEntity();

                if (pos.equals(selectedPos)) {
                    // 同一の選択なので選択を解除する
                    selection.clearSelection();
                    playerIn.sendMessage(new TextComponentTranslation("gts.message.chat.deselected", pos));
                }
                else {
                    // 選択状態にする
                    selection.setSelectedTileEntity(pos);
                    playerIn.sendMessage(new TextComponentTranslation("gts.message.chat.selected", pos));
                }
            }
        }
        else {
            if (!worldIn.isRemote) playerIn.openGui(GTS.instance, GTSGuiHandler.GUI_TRAFFIC_SPEAKER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        // 制御機のアタッチを解除する
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof GTSTileEntityTrafficSpeaker) {
            GTSTileEntityTrafficSpeaker gte = (GTSTileEntityTrafficSpeaker) te;
            if (gte.isAttached()) {
                BlockPos controllerPos = gte.getAttachedTrafficController();
                TileEntity te2 = worldIn.getTileEntity(controllerPos);
                if (te2 instanceof GTSTileEntityTrafficController) {
                    GTSTileEntityTrafficController gte2 = (GTSTileEntityTrafficController) te2;
                    gte2.deattach(gte);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }
}
