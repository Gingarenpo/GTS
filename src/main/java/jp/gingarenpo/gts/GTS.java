package jp.gingarenpo.gts;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Minecraft 1.12.2の世界に、交通システムの概念を追加する総合Mod。
 * ここはModのメインエントリーファイル。
 * @author Gingarenpo
 * @version 2.0
 */
@Mod(
		modid = GTS.MOD_ID,
		name = GTS.MOD_NAME,
		version = GTS.VERSION
)
public class GTS {
	/**
	 * GTSのmodid。1.0と基本構造は変わっていないため、1.0との共存は基本的に禁止する。
	 * 前提Modを要求しない（GingaCoreをセットにしてある）。
	 */
	public static final String MOD_ID = "gts";

	/**
	 * Mod自体の名前。一覧に表示されるときのもの。
	 */
	public static final String MOD_NAME = "GTS";

	/**
	 * Modのバージョン。
	 */
	public static final String VERSION = "2.0";

	/**
	 * Forgeによって作製される、自分自身のインスタンス。ここがNULLになることはない。
	 * This is the instance of your mod as created by Forge. It will never be null.
	 */
	@Mod.Instance(MOD_ID)
	public static GTS INSTANCE;

	/**
	 * 初期化フェーズ。TileEntityなどの登録は基本的にここで行う。
	 * This is the first initialization event. Register tile entities here.
	 * The registry events below will have fired prior to entry to this method.
	 */
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {

	}

	/**
	 * レシピとかを登録するときのフェーズ。
	 * This is the second initialization event. Register custom recipes
	 */
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {

	}

	/**
	 * 全てのModが読み込まれた後にてんやわんやするフェーズ。
	 * This is the final initialization event. Register actions from other mods here
	 */
	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {

	}

	/**
	 * Forgeはここに記されているブロックを読み込んで登録することができる。
	 *
	 * Forge will automatically look up and bind blocks to the fields in this class
	 * based on their registry name.
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Blocks {
      /*
          public static final MySpecialBlock mySpecialBlock = null; // placeholder for special block below
      */
	}

	/**
	 * 上記のアイテム版。
	 * Forge will automatically look up and bind items to the fields in this class
	 * based on their registry name.
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Items {
      /*
          public static final ItemBlock mySpecialBlock = null; // itemblock for the block above
          public static final MySpecialItem mySpecialItem = null; // placeholder for special item below
      */
	}

	/**
	 * アイテムや、ブロックを実際に登録する為のメソッドたち。
	 * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
	 */
	@Mod.EventBusSubscriber
	public static class ObjectRegistryHandler {
		/**
		 * Listen for the register event for creating custom items
		 */
		@SubscribeEvent
		public static void addItems(RegistryEvent.Register<Item> event) {
           /*
             event.getRegistry().register(new ItemBlock(Blocks.myBlock).setRegistryName(MOD_ID, "myBlock"));
             event.getRegistry().register(new MySpecialItem().setRegistryName(MOD_ID, "mySpecialItem"));
            */
		}

		/**
		 * Listen for the register event for creating custom blocks
		 */
		@SubscribeEvent
		public static void addBlocks(RegistryEvent.Register<Block> event) {
           /*
             event.getRegistry().register(new MySpecialBlock().setRegistryName(MOD_ID, "mySpecialBlock"));
            */
		}
	}

}
