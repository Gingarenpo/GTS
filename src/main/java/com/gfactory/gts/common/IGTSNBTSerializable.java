package com.gfactory.gts.common;

import net.minecraft.nbt.NBTTagCompound;

/**
 * NBTを用いた読み書き（シリアライズ）が可能なものにつけられるインターフェース。
 * このインターフェースがついたものはNBTタグにシリアライズ・デシリアライズが完全に可能で、
 * 元のインスタンスを完全に復元することができる。
 *
 * 読み書き用のメソッドを実装する必要がある。
 */
public interface IGTSNBTSerializable {

    /**
     * タグの中身を読み取り、このインスタンスに情報を反映する。
     * @param compound タグ
     */
    void readFromNBT(NBTTagCompound compound);

    /**
     * このインスタンスを表すタグを作成し、NBTにシリアライズして返す。
     * @return このインスタンスの情報を書き込んだタグ
     */
    NBTTagCompound writeToNBT();
}
