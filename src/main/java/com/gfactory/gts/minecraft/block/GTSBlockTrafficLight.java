package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.common.capability.IGTSSelection;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficLight;
import com.gfactory.gts.minecraft.item.GTSItems;
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

/**
 * 交通信号機を設置するためのブロック
 */
public class GTSBlockTrafficLight extends GTSBlock<GTSTileEntityTrafficLight> {

    public GTSBlockTrafficLight() {
        super(GTSTileEntityTrafficLight.class);
        this.setRegistryName("traffic_light");
        this.setUnlocalizedName("traffic_light");
    }

    /**
     * このブロックがクリックされたときの動作。引数が多いが使うものと使わないものがある。
     * 「アタッチ」を持っている状態の場合、選択状態に入る。そうでない場合、GUIを表示する。
     * @param worldIn 世界。
     * @param pos 座標。
     * @param state ブロックの状態。
     * @param playerIn プレイヤー。
     * @param hand どっちの手で操作したか。
     * @param facing 向き。
     * @param hitX 当たったところの小数点座標
     * @param hitY Y
     * @param hitZ Z
     * @return アクションを実行するならtrue。手を振る
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return false; // サーバーでは何も行わない
        if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return false;
        if (hand != EnumHand.MAIN_HAND) return false;

        // プレイヤーが持っているアイテムを取得
        ItemStack item = playerIn.getHeldItem(hand);

        // アタッチメント持っている場合
        if (item.isItemEqual(new ItemStack(GTSItems.ATTACHMENT)) ) {
            // 選択状態を保持するキャパビリティを取得
            IGTSSelection selection = playerIn.getCapability(GTSCapabilities.SELECTION_CAP, null);
            if (selection == null) return false;

            // 既に選択されているものと一致したら解除
            if (selection.getSelectedTileEntity() != null && selection.getSelectedTileEntity().equals(pos)) {
                selection.clearSelection();
                playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deselected", pos.toString())));
                return true;
            }

            // まあ普通は信号機のTileEntityがあるはずだけどラグったりしてなかった時を踏まえてとりあえずエラーチェックはしておく
            TileEntity te = worldIn.getTileEntity(pos);
            if (!(te instanceof GTSTileEntityTrafficLight)) return false;
            GTSTileEntityTrafficLight tet = (GTSTileEntityTrafficLight) te;


            // 登録
            selection.setSelectedTileEntity(pos);
            playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.selected", tet.getPos().toString())));

            return true;
        }
        else if (!item.isEmpty()) return true;

        // GUIを開く
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficLight)) return false;
        GTSTileEntityTrafficLight tet = (GTSTileEntityTrafficLight) te;
        Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficLight(tet));

        return true;
    }

    /**
     * このブロックが破壊された（原因問わず）直後に呼び出される。このメソッドでBlockContainerは
     * 自身に紐づけられたTileEntityの解除を行うため、このメソッドが呼ばれた直後はまだTileEntityが生き残る。
     * superを呼び出さないと怨霊が残るので注意。やるべきことをやった後にsuper()を呼び出してあげる必要がある。
     *
     * ここでは、アタッチされた交通信号制御機を取得し、そのアタッチされた交通信号制御機対しこの信号機のアタッチを外す。
     * これを行わないと色々バグる。
     * @param worldIn 世界
     * @param pos 座標
     * @param state 状態
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        // 普通ないがバグってその場所にTileEntityがもういないとか別のものになってるとかで落ちるのが嫌なのでエラーチェック
        if (te instanceof GTSTileEntityTrafficLight) {
            GTSTileEntityTrafficLight tet = (GTSTileEntityTrafficLight) te;
            // その場所にあるとされている制御機のTileEntityを取得するが、ないかもしれないし別のものかもしれないのでエラーチェック
            if (tet.getAttachedTrafficController() != null) {
                TileEntity te2 = worldIn.getTileEntity(tet.getAttachedTrafficController());
                if (te2 instanceof GTSTileEntityTrafficController) {
                    ((GTSTileEntityTrafficController) te2).deattach(tet);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}
