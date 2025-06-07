package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.common.capability.IGTSSelection;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficController;
import com.gfactory.gts.minecraft.item.GTSItems;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficLight;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class GTSBlockTrafficContoller extends GTSBlock<GTSTileEntityTrafficController> {

    public GTSBlockTrafficContoller() {
        super(GTSTileEntityTrafficController.class);
        this.setRegistryName("traffic_controller");
        this.setUnlocalizedName("traffic_controller");
    }

    /**
     * このブロックが破壊された（原因問わず）直後に呼び出される。このメソッドでBlockContainerは
     * 自身に紐づけられたTileEntityの解除を行うため、このメソッドが呼ばれた直後はまだTileEntityが生き残る。
     * superを呼び出さないと怨霊が残るので注意。やるべきことをやった後にsuper()を呼び出してあげる必要がある。
     *
     * ここでは、アタッチされた交通信号機を取得し、そのアタッチされた交通信号機全てに対しこの制御機のアタッチを外す。
     * これを行わないと色々バグる。
     * @param worldIn 世界
     * @param pos 座標
     * @param state 状態
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        // 普通大丈夫だと思うけど万が一のことを考えてエラーチェック
        if (te instanceof GTSTileEntityTrafficController) {
            GTSTileEntityTrafficController tet = (GTSTileEntityTrafficController) te;
            for (BlockPos p: tet.getAttachedTrafficLights()) {
                // その場所に位置するTileEntityを取得する。バグってnullかもしれないし全然違うものが返ってくる場合もある
                TileEntity te2 = worldIn.getTileEntity(p);
                if (!(te2 instanceof GTSTileEntityTrafficLight)) continue; // 無視
                GTSTileEntityTrafficLight tet2 = (GTSTileEntityTrafficLight) te2;
                tet2.deattach(tet); // デアタッチ
                if (!worldIn.isRemote) worldIn.notifyBlockUpdate(p, worldIn.getBlockState(p), worldIn.getBlockState(p), 15);
            }
            tet.getAttachedTrafficLights().clear();
            for (BlockPos p: tet.getAttachedTrafficButtons()) {
                // その場所に位置するTileEntityを取得する。バグってnullかもしれないし全然違うものが返ってくる場合もある
                TileEntity te2 = worldIn.getTileEntity(p);
                if (!(te2 instanceof GTSTileEntityTrafficButton)) continue; // 無視
                GTSTileEntityTrafficButton tet2 = (GTSTileEntityTrafficButton) te2;
                tet2.deattach(tet); // デアタッチ
                if (!worldIn.isRemote) worldIn.notifyBlockUpdate(p, worldIn.getBlockState(p), worldIn.getBlockState(p), 15);
            }
            tet.getAttachedTrafficButtons().clear();
            if (!worldIn.isRemote) worldIn.notifyBlockUpdate(te.getPos(), worldIn.getBlockState(te.getPos()), worldIn.getBlockState(tet.getPos()), 15);

        }
        super.breakBlock(worldIn, pos, state);
    }

    /**
     * このブロックが何者かによって右クリックされた場合に呼び出される。
     * 通常はプレイヤーで、交通信号制御機の場合はGUIを呼び出すか交通信号機とアタッチする。
     * 交通信号機とアタッチする条件はアタッチメントを持っていて且つ既に交通信号機がセレクトされていること。そうでない場合は
     * GUI選択とする。
     * @param worldIn 世界
     * @param pos 座標
     * @param state 状態
     * @param playerIn 右クリックしたプレイヤー
     * @param hand どっちの手？
     * @param facing 右クリックされた面
     * @param hitX 小数座標
     * @param hitY 小数座標
     * @param hitZ 小数座標
     * @return TRUEを返すと手を振る
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return false; // サーバーでは何も行わない
        if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return false;
        // アイテムを持っているかチェックする（それ以外はGUIなのでアーリーリターンが使えずifのネストになっている
        ItemStack item = playerIn.getHeldItem(hand);
        if (item.isItemEqual(new ItemStack(GTSItems.ATTACHMENT))) {
            // アタッチメントを持っている場合は、選択状態にあるかチェックする
            IGTSSelection selection = playerIn.getCapability(GTSCapabilities.SELECTION_CAP, null);
            if (selection != null && selection.getSelectedTileEntity() != null) {
                // 選択されているものが交通信号機かチェックする
                TileEntity te = worldIn.getTileEntity(selection.getSelectedTileEntity());
                if (te instanceof GTSTileEntityTrafficLight) {
                    // この交通信号機をアタッチするため、ここにあるTileEntityを取得する
                    // ないとは思うがTileEntityが制御機のものか確認する
                    TileEntity te2 = worldIn.getTileEntity(pos);
                    if (te2 instanceof GTSTileEntityTrafficController) {
                        // もうif文の嵐だが、既にある場合はデタッチ、ない場合はアタッチする
                        GTSTileEntityTrafficController tet = (GTSTileEntityTrafficController) te2;
                        if (tet.getAttachedTrafficLights().contains(selection.getSelectedTileEntity())) {
                            tet.deattach((GTSTileEntityTrafficLight) te);
                            ((GTSTileEntityTrafficLight) te).deattach(tet);
                            playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deattached", pos, selection.getSelectedTileEntity())));
                        } else {
                            playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.attached", pos, selection.getSelectedTileEntity())));
                            tet.attach((GTSTileEntityTrafficLight) te);
                            ((GTSTileEntityTrafficLight) te).attach(tet);
                        }
                        selection.clearSelection();
                        return true;
                    }
                }
                if (te instanceof GTSTileEntityTrafficButton) {
                    // この押ボタン箱をアタッチするため、ここにあるTileEntityを取得する
                    // ないとは思うがTileEntityが制御機のものか確認する
                    TileEntity te2 = worldIn.getTileEntity(pos);
                    if (te2 instanceof GTSTileEntityTrafficController) {
                        // もうif文の嵐だが、既にある場合はデタッチ、ない場合はアタッチする
                        GTSTileEntityTrafficController tet = (GTSTileEntityTrafficController) te2;
                        if (tet.getAttachedTrafficButtons().contains(selection.getSelectedTileEntity())) {
                            tet.deattach((GTSTileEntityTrafficButton) te);
                            ((GTSTileEntityTrafficButton) te).deattach(tet);
                            playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deattached", pos, selection.getSelectedTileEntity())));
                        } else {
                            playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.attached", pos, selection.getSelectedTileEntity())));
                            tet.attach((GTSTileEntityTrafficButton) te);

                            ((GTSTileEntityTrafficButton) te).attach(tet);
                        }
                        selection.clearSelection();
                        return true;
                    }
                }
            }
            return true;
        }

        // そうでない場合はGUIを開く
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficController)) return false; // TileEntityとして不適切
        Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficController((GTSTileEntityTrafficController) te));
        return true;
    }
}
