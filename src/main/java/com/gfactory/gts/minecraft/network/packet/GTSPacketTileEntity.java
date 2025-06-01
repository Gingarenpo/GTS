package com.gfactory.gts.minecraft.network.packet;

import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * モデルの変更をサーバーとクライアントで送受信するためのクラス
 * 役割別に書いておかないと諸々困る
 */
public class GTSPacketTileEntity<T extends GTSTileEntity> implements IMessage, IMessageHandler<GTSPacketTileEntity<T>, IMessage> {
    private Class<T> clazz;
    private NBTTagCompound c;
    private BlockPos p;

    public GTSPacketTileEntity() {}

    public GTSPacketTileEntity(NBTTagCompound te, BlockPos p, Class<T> clazz) {
        this.c = te;
        this.p = p;
        this.clazz = clazz;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.c = ByteBufUtils.readTag(buf);
        this.p = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.c);
        buf.writeLong(this.p.toLong());
    }

    @Override
    public IMessage onMessage(GTSPacketTileEntity<T> message, MessageContext ctx) {
        World world = ctx.getServerHandler().player.getServerWorld();
        TileEntity t = world.getTileEntity(message.p);
        if (t instanceof GTSTileEntity) {
            GTSTileEntity te = (GTSTileEntity) t;
            te.readFromNBT(message.c);
            te.markDirty();
            // world.notifyBlockUpdate(t.getPos(), world.getBlockState(t.getPos()), world.getBlockState(t.getPos()), 3);
        }
        return null;
    }
}
