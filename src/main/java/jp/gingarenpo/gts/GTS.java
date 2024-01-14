package jp.gingarenpo.gts;

import jp.gingarenpo.gts.base.GTSGuiHandler;
import jp.gingarenpo.gts.control.BlockTrafficController;
import jp.gingarenpo.gts.control.RendererTrafficController;
import jp.gingarenpo.gts.control.TileEntityTrafficController;
import jp.gingarenpo.gts.pack.Loader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

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
	public static final String MOD_NAME = "GTS - Ginren Traffic System";

	/**
	 * Modのバージョン。
	 */
	public static final String VERSION = "2.0";

	/**
	 * GTSのロガー。
	 */
	public static final Logger LOGGER = LogManager.getLogger("GTS");

	/**
	 * GTSのアドオンを読みだすためのローダー。
	 */
	public static final Loader LOADER = new Loader();

	/**
	 * GTSのアドオンを格納しているディレクトリの位置。
	 */
	public static File GTS_MOD_DIR;

	/**
	 * GTSのブロックやアイテムを格納するためのタブ。
	 */
	public static CreativeTabs TAB;

	/**
	 * パケット通信に使用する簡易的なネットワークアダプタみたいなもの。
	 */
	public static SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

	/**
	 * Forgeによって作製される、自分自身のインスタンス。ここがNULLになることはない。
	 * This is the instance of your mod as created by Forge. It will never be null.
	 */
	@Mod.Instance(MOD_ID)
	public static GTS INSTANCE;

	/**
	 * ダミーパックの名称。
	 */
	public static final String DUMMY_PACK_NAME = "___Dummy___";

	/**
	 * 初期化フェーズ。TileEntityなどの登録は基本的にここで行う。
	 * This is the first initialization event. Register tile entities here.
	 * The registry events below will have fired prior to entry to this method.
	 */
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		// アドオンの読み込みを開始する
		GTS_MOD_DIR = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath() + "\\mods\\GTS"); // 場所を指定
		LOADER.search(GTS_MOD_DIR); // 検索を開始

		// TileEntityを登録する
		GameRegistry.registerTileEntity(TileEntityTrafficController.class, new ResourceLocation(MOD_ID, "traffic_controller"));

		// GUIハンドラの登録
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GTSGuiHandler());

		// TESRの登録
		this.registerTESRs();


	}

	/**
	 * レシピとかを登録するときのフェーズ。
	 * This is the second initialization event. Register custom recipes
	 */
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		// タブを登録する
		TAB = new CreativeTabs("gts_tab") {
			@Override
			public ItemStack createIcon() {
				return new ItemStack(Items.traffic_controller);
			}
		};
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
	 * ブロック登録イベントが発火されたとき、大文字を小文字にしたうえで同名のフィールドを探してそこにインスタンスを入れてくれるようだ。
	 * 一致しないと「Unable to lookup」が出る
	 *
	 * Forge will automatically look up and bind blocks to the fields in this class
	 * based on their registry name.
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Blocks {
      public static final BlockTrafficController traffic_controller = null;
	}

	/**
	 * 上記のアイテム版。
	 * Forge will automatically look up and bind items to the fields in this class
	 * based on their registry name.
	 */
	@GameRegistry.ObjectHolder(MOD_ID)
	public static class Items {
      public static final ItemBlock traffic_controller = null;
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
			System.out.println("ADD ITEM!");
			event.getRegistry().registerAll(
					// 制御機ブロック（アイテム）を追加
					new ItemBlock(Blocks.traffic_controller).setRegistryName(MOD_ID, "traffic_controller")
			);
		}

		/**
		 * Listen for the register event for creating custom blocks
		 */
		@SubscribeEvent
		public static void addBlocks(RegistryEvent.Register<Block> event) {
			System.out.println("ADD BLOCK!");
			event.getRegistry().registerAll(
					// 制御機ブロックを追加
					new BlockTrafficController().setRegistryName(MOD_ID, "traffic_controller")
			);
		}

		/**
		 * モデルを登録するイベント（クライアントサイドのみで発火する）。
		 * これを行わないとJSONを読み込んでくれない？（FileNotFoundExceptionが出る）
		 */
		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			ModelLoader.setCustomModelResourceLocation(Items.traffic_controller, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(Items.traffic_controller.getRegistryName(), "inventory"));
		}
	}

	/**
	 * TileEntitySpecialRendererを登録するメソッド
	 * これは、クライアントサイドからしか呼んではならないため別メソッド化。
	 */
	@SideOnly(Side.CLIENT)
	public void registerTESRs() {
		// 制御機のレンダー
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTrafficController.class, new RendererTrafficController());
	}

}
