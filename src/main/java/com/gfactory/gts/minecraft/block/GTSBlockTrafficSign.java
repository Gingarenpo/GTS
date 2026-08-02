package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.minecraft.gui.sign.GTSGui114Sign;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 地名板、標示板などを追加するためのブロック
 */
public class GTSBlockTrafficSign extends GTSBlock<GTSTileEntityTrafficSign> {

    public GTSBlockTrafficSign() {
        super(GTSTileEntityTrafficSign.class);
        this.setRegistryName("traffic_sign");
        this.setUnlocalizedName("traffic_sign");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return false; // サーバーでは何も行わない
        if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return false;
        if (!playerIn.getHeldItem(hand).isEmpty()) return false;

        // GUI開く
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficSign)) return false;
        Minecraft.getMinecraft().displayGuiScreen(new GTSGui114Sign((GTSTileEntityTrafficSign) te));
        return true;
    }
}
