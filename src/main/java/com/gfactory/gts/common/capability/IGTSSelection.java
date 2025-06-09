package com.gfactory.gts.common.capability;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.math.BlockPos;

/**
 * プレイヤーの選択状況を保持するためのもの。
 * このインターフェースを実装することで、今選んでいる場所を格納することができる。
 */
public interface IGTSSelection {

    /**
     * 現在のプレイヤーが選択状態にしているTileEntityの座標を取得する。
     * 何も持っていない場合はNULLを返す。
     * @return 選択状態のTileEntity
     */
    BlockPos getSelectedTileEntity();

    /**
     * 現在のプレイヤーに指定した座標のTileEntityを持たせたことにする。
     */
    void setSelectedTileEntity(BlockPos pos);

    /**
     * 選択状態を解除する。
     */
    void clearSelection();
}
