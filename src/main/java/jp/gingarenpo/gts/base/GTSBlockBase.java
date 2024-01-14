package jp.gingarenpo.gts.base;

import jp.gingarenpo.gts.GTS;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * GTSで使用する機械類を設置するためのブロックのベース部分のクラス。
 * 抽象クラスのため、このクラスを使用してインスタンスを生成することはできない。
 * タブの設定などをあらかじめ設定しておく。
 */
public abstract class GTSBlockBase extends BlockContainer {

	/**
	 * このブロックのインスタンスを作成する。
	 * 材質は固定となる。
	 *
	 */
	public GTSBlockBase() {
		super(Material.ROCK);
		this.setHardness(999999);
	}

	/**
	 * GTSで使用するブロックは全て共通のタブを返すこととする。
	 * そのクリエイティブタブを返すためのメソッド。
	 * @return クリエイティブタブ。
	 */
	@Override
	public CreativeTabs getCreativeTab() {
		return GTS.TAB;
	}


}
