package jp.gingarenpo.gts.control;

import jp.gingarenpo.gts.GTS;
import jp.gingarenpo.gts.GTSGuiId;
import jp.gingarenpo.gts.base.GTSBlockBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTrafficController extends GTSBlockBase {

	/**
	 * このブロックのインスタンスを作成する。
	 * 材質は固定となる。
	 *
	 */
	public BlockTrafficController() {
		super();
		this.setTranslationKey("traffic_controller");
	}

	/**
	 * ワールドに存在するTileEntityを生成する。
	 * ブロックが設置されたときに同時に紐づけられる。
	 *
	 * @param worldIn 置かれた世界。
	 * @param meta メタデータ。
	 * @return 紐づかれるTileEntity。
	 */
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}

	/**
	 * 引数が大変多いが、ブロックを右クリックしたときに起こるアクションをここで指定する。
	 * イベントリスナとなっている。
	 * 今回の場合、制御機のGUIを起動する。
	 *
	 * @param worldIn ブロックの置かれている世界。
	 * @param pos ブロックの置かれている世界座標。
	 * @param state ブロックの状態。
	 * @param playerIn ブロックを右クリックしたプレイヤー。
	 * @param hand ブロックをどちらの手でクリックしたか。
	 * @param facing クリックされたブロックの面の向き。
 	 * @param hitX クリックされたブロックのX位置。0-1の間で返される。
	 * @param hitY クリックされたブロックのY位置。
	 * @param hitZ クリックされたブロックのZ位置。
	 * @return 何かしらのアクションを起こしたのであればtrue。何も動作しないならばfalse。trueにすると手が動く。
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		playerIn.openGui(GTS.INSTANCE, GTSGuiId.TRAFFIC_CONTROLLER.getId(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
}
