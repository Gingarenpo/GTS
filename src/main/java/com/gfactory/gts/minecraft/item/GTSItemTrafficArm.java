package com.gfactory.gts.minecraft.item;

import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.gui.GTSGuiTrafficArm;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficArm;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
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
        GTSTileEntityTrafficArm te = new GTSTileEntityTrafficArm();
        if (compound != null) {
            // タグはないこともあるので一旦あるかどうか確認。あれば中身を読み込む
            te.readFromNBT(compound);
        }

        // GUIを開く
        Minecraft.getMinecraft().displayGuiScreen(new GTSGuiTrafficArm(te, playerIn.getHeldItem(handIn), playerIn.inventory.currentItem));

        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
