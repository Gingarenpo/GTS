package com.gfactory.gts.common.capability;

import net.minecraft.util.math.BlockPos;

/**
 * インターフェースをそのまま実装しただけ
 */
public class GTSSelection implements IGTSSelection {

    /**
     * 選択された場所を保持しておくための場所
     */
    private BlockPos selectedTileEntity;

    @Override
    public BlockPos getSelectedTileEntity() {
        return this.selectedTileEntity;
    }

    @Override
    public void setSelectedTileEntity(BlockPos pos) {
        this.selectedTileEntity = pos;
    }

    @Override
    public void clearSelection() {
        this.selectedTileEntity = null;
    }
}
