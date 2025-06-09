package com.gfactory.gts.minecraft.item;

import com.gfactory.gts.common.capability.GTSCapabilities;
import com.gfactory.gts.common.capability.IGTSSelection;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficArm;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficArm;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * アームのアイテム。
 * GTSでは、アームはポールの1種として設置されるため、アイテムとしてのみ保持し
 * ブロックにはしない。ただしこちらもモデルの変更は可能。アイテムの為空中クリックで
 * 指定することになる。そのためモデルの持ち方がやや特殊。
 */
public class GTSItemTrafficArm extends Item {

    public GTSItemTrafficArm() {
        this.setRegistryName("gts", "traffic_arm");
        this.setUnlocalizedName("gts.traffic_arm");
        this.setCreativeTab(GTS.TAB);
    }

    /**
     * このアイテムを右クリックしたときに呼び出されるイベント。
     * アームを何もないところでクリックした場合に呼び出されるもの。カーソルがあるとどうなるかは未知数。
     * ここでは、モデル選択画面を呼び出す。
     * @param worldIn 世界
     * @param playerIn 使ったプレイヤー
     * @param handIn どっちの手？
     * @return アクションリザルト？
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!worldIn.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));

        // プレイヤーの持ち物からデータを取得する
        NBTTagCompound compound = playerIn.getHeldItem(handIn).getTagCompound();
        GTSTileEntityTrafficArm te = new GTSTileEntityTrafficArm(null);
        if (compound != null) {
            // タグはないこともあるので一旦あるかどうか確認。あれば中身を読み込む
            te.readFromNBT(compound);
        }

        // GUIを開く
        Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficArm(te, playerIn.getHeldItem(handIn), playerIn.inventory.currentItem));

        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    /**
     * このアイテムをブロックに対して使用したときに発動する。
     * 何も選択されておらず、かつポールを選んだ場合はそのポールを選択対象に加える。
     * ポールが選択されていて、何かしらのブロックを指定した場合はそのブロックを最終地点として登録する。
     * 同じ場所を選んだ場合は選択を解除する。その他の場合は何もしない。
     *
     * @param player ブロックに対してアイテムを使用したプレイヤー
     * @param worldIn 世界
     * @param pos　座標
     * @param hand どっちの手？
     * @param facing どの向き？
     * @param hitX 小数座標
     * @param hitY 小数座標
     * @param hitZ 小数座標
     * @return SUCCESSで完了、PASSは多分次のイベント発火、CANCELはそのまんま
     */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) return EnumActionResult.PASS; // サーバーの場合は無視

        // アイテムのタグが登録されていなければ登録
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = stack.getTagCompound();
        GTSTileEntityTrafficArm arm = new GTSTileEntityTrafficArm(null);
        if (compound != null) arm.readFromNBT(compound);

        // 現在のプレイヤーキャパビリティを取得
        IGTSSelection selection = player.getCapability(GTSCapabilities.SELECTION_CAP, null);
        if (selection == null) return EnumActionResult.PASS; // 存在しない場合は無視
        BlockPos selectedPos = selection.getSelectedTileEntity();

        // 指定したブロックのTileEntityを取得
        TileEntity end = worldIn.getTileEntity(pos);

        // 指定したTileEntityがポールであり、かつ未選択の場合はそれを登録する
        if (end instanceof GTSTileEntityTrafficPole && selectedPos == null) {
            selection.setSelectedTileEntity(pos);
            player.sendMessage(new TextComponentString(I18n.format("gts.message.chat.selected", pos)));
        }
        // 指定したTileEntityがポールであり、選択されているポールと同一座標の場合は解除する
        else if (end instanceof GTSTileEntityTrafficPole && selectedPos.equals(pos)) {
            selection.clearSelection();
            player.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deselected", pos)));
        }
        else if (selectedPos != null) {
            // 始点選択対象のTileEntityを取得
            TileEntity selectedTileEntity = worldIn.getTileEntity(selectedPos);

            // 始点選択対象がポールでない場合（例えば信号機とか）、何もしない（信号機を解除されるのも困るので）
            if (!(selectedTileEntity instanceof GTSTileEntityTrafficPole)) return EnumActionResult.PASS;

            // 始点選択対象がポールの場合、その座標を登録するが、既に登録されている場合は解除する
            GTSTileEntityTrafficPole pole = (GTSTileEntityTrafficPole) selectedTileEntity;
            arm.setPos(pos);
            if (pole.getJointArms().contains(arm)) {
                pole.deattach(arm);
                player.sendMessage(new TextComponentString(I18n.format("gts.message.chat.deattached", pos, selectedPos)));
                selection.clearSelection();
            }
            else {
                pole.attach(arm);
                player.sendMessage(new TextComponentString(I18n.format("gts.message.chat.attached", pos, selectedPos)));
                selection.clearSelection();
            }

        }
        return EnumActionResult.SUCCESS;
    }
}
