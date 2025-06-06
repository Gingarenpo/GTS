package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.minecraft.gui.GTSGuiTrafficPole;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 何も信号機だけに限らず使用できる、ポール（信号柱）。
 * フランジポールみたいなものにも対応できるように、モデルパックが対応すれば
 * 横方向にも接続できるように対応予定。オブジェクトを作ればよろし。
 *
 * ポールそのものには特段機能はない
 */
public class GTSBlockTrafficPole extends GTSBlock<GTSTileEntityTrafficPole> {

    public GTSBlockTrafficPole() {
        super(GTSTileEntityTrafficPole.class);
        this.setRegistryName("traffic_pole");
        this.setUnlocalizedName("traffic_pole");
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficPole)) return;

        GTSTileEntityTrafficPole self = (GTSTileEntityTrafficPole) te;

        // 上下のTileEntityを取得
        TileEntity top = worldIn.getTileEntity(pos.up());
        TileEntity bottom = worldIn.getTileEntity(pos.down());

        // 自分自身のフラグを設定
        self.setUpJoint(top instanceof GTSTileEntityTrafficPole);
        self.setBottomJoint(bottom instanceof GTSTileEntityTrafficPole);

        self.markDirty();
        worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 15);
    }

    /**
     * 元々ここにアームがクリックされた時を入れようとしていたが、アームに責務を持たせるようにしたためから実装
     * @param worldIn
     * @param pos
     * @param state
     * @param playerIn
     * @param hand
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return false; // サーバーでは何も行わない
        if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return false;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficPole)) return false;

        // GUIを出す
        Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficPole((GTSTileEntityTrafficPole) te));

        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) return; // クライアント側でのチェックはしない
        if (pos.getY() == fromPos.getY()) return; // 左右の検知は無視

        // 自分自身のTileEntityを取得
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficPole)) return; // そんなことないだろうが
        GTSTileEntityTrafficPole self = (GTSTileEntityTrafficPole) te;

        // 上下のTileEntityを取得
        TileEntity top = worldIn.getTileEntity(pos.up());
        TileEntity bottom = worldIn.getTileEntity(pos.down());

        // 自分自身のフラグを設定
        self.setUpJoint(top instanceof GTSTileEntityTrafficPole);
        self.setBottomJoint(bottom instanceof GTSTileEntityTrafficPole);

        self.markDirty();
        worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 15);
    }

}
