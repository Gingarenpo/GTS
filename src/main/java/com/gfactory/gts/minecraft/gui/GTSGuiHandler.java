package com.gfactory.gts.minecraft.gui;

import com.gfactory.gts.minecraft.gui.sign.GTSGui114Sign;
import com.gfactory.gts.minecraft.tileentity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * サーバー対応に伴い、クライアント側のGUIを直接Newで更新すると予期せぬエラーが発生するため
 * GUIをネットワーク経由でパケット送信して呼び出す仕組みに変更する。
 * それによりサーバー側ではGUIを開かなくて済む。
 */
public class GTSGuiHandler implements IGuiHandler {

    public static final int GUI_TRAFFIC_LIGHT = 0;
    public static final int GUI_TRAFFIC_CONTROLLER = 1;
    public static final int GUI_TRAFFIC_POLE = 2;
    public static final int GUI_TRAFFIC_ARM = 3;
    public static final int GUI_TRAFFIC_BUTTON = 4;
    public static final int GUI_TRAFFIC_SIGN = 5;
    public static final int GUI_TRAFFIC_SPEAKER = 6; // 予約
    public static final int GUI_TRAFFIC_SENSOR = 7; // 予約

    /**
     * クライアント側でGUIを返す
     * @param ID The Gui ID Number
     * @param player The player viewing the Gui
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @return
     */
    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        switch (ID) {
            case GUI_TRAFFIC_LIGHT:
                // 信号機の場合はそのまま渡す
                if (te instanceof GTSTileEntityTrafficLight) {
                    return new GTSGuiTrafficLight((GTSTileEntityTrafficLight) te);
                }
                break;
            case GUI_TRAFFIC_CONTROLLER:
                if (te instanceof GTSTileEntityTrafficController) {
                    return new GTSGuiTrafficController((GTSTileEntityTrafficController) te);
                }
                break;
            case GUI_TRAFFIC_POLE:
                if (te instanceof GTSTileEntityTrafficPole) {
                    return new GTSGuiTrafficPole((GTSTileEntityTrafficPole) te);
                }
                break;
            case GUI_TRAFFIC_ARM:
                // アームの場合は少し特殊で、TileEntityが不要（というか存在しない）
                //EnumHand hand = EnumHand.values()[x];
                ItemStack stack = player.getHeldItem(player.swingingHand);
                GTSTileEntityTrafficArm ate = new GTSTileEntityTrafficArm(null);
                if (stack.hasTagCompound()) {
                    ate.readFromNBT(stack.getTagCompound());
                }
                return new GTSGuiTrafficArm(ate, stack, player.inventory.currentItem);
            case GUI_TRAFFIC_BUTTON:
                if (te instanceof GTSTileEntityTrafficButton) {
                    return new GTSGuiTrafficButton((GTSTileEntityTrafficButton) te);
                }
                break;
            case GUI_TRAFFIC_SIGN:
                if (te instanceof GTSTileEntityTrafficSign) {
                    return new GTSGui114Sign((GTSTileEntityTrafficSign) te);
                }
                break;
            case GUI_TRAFFIC_SPEAKER:
                // 予約済み
                break;
            case GUI_TRAFFIC_SENSOR:
                // 予約済み
                break;
            default:
                // あり得ない
                break;
        }
        return null;
    }

    /**
     * サーバー側で管理するもの。今回アイテムスロットを同期する必要がないためダミーのコンテナ返す
     * @param ID The Gui ID Number
     * @param player The player viewing the Gui
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @return
     */
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GTSGuiDummyContainer();
    }
}
