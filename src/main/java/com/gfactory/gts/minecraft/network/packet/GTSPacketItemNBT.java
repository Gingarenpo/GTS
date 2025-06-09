package com.gfactory.gts.minecraft.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * アームに代表される、アイテムのNBTタグをプレイヤーがいる際に同期するためのパケットを送信する。
 * これによりクライアント側の者がサーバーに同期される。
 */
public class GTSPacketItemNBT implements IMessage, IMessageHandler<GTSPacketItemNBT, IMessage> {

    /**
     * プレイヤーのホットバーの同期スロット（0-8）
     */
    private int slot;

    /**
     * 同期するタグ
     */
    private NBTTagCompound compound;

    public GTSPacketItemNBT() {
    }

    public GTSPacketItemNBT(int slot, NBTTagCompound compound) {
        this.compound = compound;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        ByteBufUtils.writeTag(buf, this.compound);
    }

    @Override
    public IMessage onMessage(GTSPacketItemNBT message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().player;
        if (player != null && message.slot >= 0 && message.slot < player.inventory.mainInventory.size()) {
            // プレイヤーがいてスロットが正しければ、そのスロットのアイテムに追加する
            ItemStack stack = player.inventory.getStackInSlot(message.slot);
            if (!stack.isEmpty()) {
                // アイテムが空でなければ追加する（ただそのアイテムはもうクライアントが持っているアイテムじゃないかもしれないが
                // そこまで同期が遅延していることはそうそうないはず
                stack.setTagCompound(message.compound);
            }
        }
        return null;
    }
}
