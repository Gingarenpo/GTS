package com.gfactory.gts.minecraft.tileentity;

import com.gfactory.gts.pack.config.GTSConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * TileEntityを使用しなくてはならないものの、Itemに付与するなどの理由で
 * TileEntityとしての役割を果たさないもの。だがこれはこれで持ち方としてはありなので
 * アイテムで使用する場合はこのクラスを継承させる。ポリモーフィズムの観点からは
 * ツッコまれそうだが、データを全部切り出すのは拷問なのでこれで勘弁する。
 * なのでこのTileEntityを使用して何かを送受信することはできない。あくまでデータのラッピング
 */
public abstract class GTSTileEntityDummy<T extends GTSConfig> extends GTSTileEntity<GTSConfig> {

    /**
     * このTileEntityは実体を持たないため、posはNULLを返す。
     * @return 必ずNULL
     */
    @Override
    public BlockPos getPos() {
        return null;
    }

    /**
     * セットしても何も起きない
     * @param posIn
     */
    @Override
    public void setPos(BlockPos posIn) {
        // NOOP
    }

    /**
     * 世界を持たないためNULLを返す
     * @return
     */
    @Override
    public World getWorld() {
        return super.getWorld();
    }

    @Override
    public void setWorld(World worldIn) {
        // NOOP
    }
}
