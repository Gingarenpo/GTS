package com.gfactory.gts.minecraft.item;

import com.gfactory.gts.minecraft.GTS;
import net.minecraft.item.Item;

/**
 * 交通信号制御機と交通信号機をアタッチするためのアイテム。WorldEditで言うところのコマンドで出てくる魔法のシャベルみたいなやつ
 */
public class GTSItemAttachment extends Item {

    public GTSItemAttachment() {
        this.setRegistryName("gts", "attachment");
        this.setUnlocalizedName("gts.attachment");
        this.setCreativeTab(GTS.TAB);
    }
}
