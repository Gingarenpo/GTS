package com.gfactory.gts.minecraft.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * ダミーのコンテナを用意し、サーバーとクライアント間での通信をなんかこういい感じにする
 * これがないとパケットエラーで不整合が起きる
 */
public class GTSGuiDummyContainer extends Container {
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
