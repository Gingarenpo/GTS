package com.gfactory.gts.minecraft.tileentity;

/**
 * TileEntityにつけることが想定されている。
 * このインターフェースをつけた場合、このTileEntityはTによってアタッチされることができる。
 *
 */
public interface IGTSAttachable<T> {

    /**
     * このTileEntityにTをアタッチする。
     * @param te アタッチされるT
     */
    void attach(T te);

    /**
     * このTileEntityからTを解除する。
     * なお、既にアタッチされていない者が来る場合もある。
     * @param te アタッチ解除するT
     */
    void deattach(T te);

    /**
     * アタッチされたリストを初期化する。
     * あまり使用しないが実装しておくこと。
     */
    void reset();

}
