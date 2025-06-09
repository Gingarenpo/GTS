package com.gfactory.gts.minecraft.block;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.common.capability.IGTSSelection;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficButton;
import com.gfactory.gts.minecraft.item.GTSItems;
import com.gfactory.gts.minecraft.sound.GTSSoundTrafficButtonDetected;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficButton;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficController;
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
 * 押ボタン箱のブロック
 */
public class GTSBlockTrafficButton extends GTSBlock<GTSTileEntityTrafficButton>{

    public GTSBlockTrafficButton() {
        super(GTSTileEntityTrafficButton.class);
        this.setRegistryName("traffic_button");
        this.setUnlocalizedName("traffic_button");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return false; // サーバーでは何も行わない
        if (hand != EnumHand.MAIN_HAND) return false; // 左手の場合は何も行わない
        if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return false;

        // TileEntityを取得
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof GTSTileEntityTrafficButton)) return false;
        GTSTileEntityTrafficButton self = (GTSTileEntityTrafficButton) te;

        // アタッチメントを持っているかどうかを取得
        ItemStack item = playerIn.getHeldItem(hand);
        if (item.isItemEqual(new ItemStack(GTSItems.ATTACHMENT))) {
            // アタッチメントを持っている場合、アタッチモードに入る
            IGTSSelection selection = playerIn.getCapability(GTSCapabilities.SELECTION_CAP, null);
            if (selection == null) return false;
            BlockPos selectedPos = selection.getSelectedTileEntity();

            if (pos.equals(selectedPos)) {
                // 同一の選択なので選択を解除する
                selection.clearSelection();
                playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deselected", pos)));
            }
            else {
                // 選択状態にする
                selection.setSelectedTileEntity(pos);
                playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.selected", pos)));
            }
        }
        else if (playerIn.isSneaking()) {
            // スニークしながらの場合は、モデル選択GUIを開く
            Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficButton(self));
        }
        else {
            // アタッチしている制御機がない場合、一応メッセージ出しておく
            if (self.getAttachedTrafficController() == null) {
                playerIn.sendMessage(new TextComponentString(I18n.format("gts.message.chat.no.attached")));
                return false;
            }

            // 既に検知状態の場合は無視
            if (self.isDetected()) return false;

            // アタッチしている制御機に対して検知信号を発信するため検知信号受信をオンにする
            self.setDetected(true);

            // 音鳴らす
            Minecraft.getMinecraft().getSoundHandler().playSound(new GTSSoundTrafficButtonDetected(self));
        }

        return true;
    }

    /**
     * このブロックが壊されたときに呼び出される。
     * TileEntityはまだある。
     * なので、ここでアタッチされている制御機から強制デアタッチする。
     * @param worldIn
     * @param pos
     * @param state
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof GTSTileEntityTrafficButton) {
            GTSTileEntityTrafficButton self = (GTSTileEntityTrafficButton) te;
            if (self.getAttachedTrafficController() != null) {
                // その制御機のアタッチ解除
                TileEntity te2 = worldIn.getTileEntity(self.getAttachedTrafficController());
                if (te2 instanceof GTSTileEntityTrafficController) {
                    GTSTileEntityTrafficController controller = (GTSTileEntityTrafficController) te2;
                    controller.deattach(self);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }
}
