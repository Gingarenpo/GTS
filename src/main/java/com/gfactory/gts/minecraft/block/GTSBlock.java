package com.gfactory.gts.minecraft.block;

import com.gfactory.core.helper.GMathHelper;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.item.GTSItems;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * 結構な割合で使う共通のものはここに残しておく。
 * 例えば固さとか。材質はとりあえず全部石とする。
 */
public abstract class GTSBlock<T extends GTSTileEntity> extends BlockContainer {

    /**
     * TileEntityとして初期化すべきクラス。Tがそのまま使えないのでこうするしかない
     */
    private final Class<T> tileEntityClass;

    public GTSBlock(Class<T> tileEntityClass) {
        super(Material.ROCK);
        this.setHardness(99999); // こわせな～い
        this.setResistance(99999); // こわれな～い
        this.setCreativeTab(GTS.TAB);
        this.tileEntityClass = tileEntityClass;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        try {
            return tileEntityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO: 普通ないと思うけど気が向いたら変える
            throw new RuntimeException("SOMETHING WENT TO WRONG!!!!!!");
        }
    }

    /**
     * 非推奨になっているがそんなの知らん。これが立方体かどうか。
     * これをfalseにしないと底が突き抜ける。
     * @param state 状態
     * @return false
     */
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * このブロックが透明かどうか。これも非推奨だが無視
     * @param state 状態
     * @return false
     */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /**
     * このブロックの当たり判定の範囲を返す。今回当たり判定あると邪魔なのでとりあえず消した。
     * @param blockState
     * @param worldIn
     * @param pos
     * @return
     */
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    /**
     * このブロックが置かれた際に呼び出される。
     * 検証結果、どうやらTileEntityが作成されてから呼び出されるようなので安全？
     * @param worldIn
     * @param pos
     * @param state
     * @param placer
     * @param stack
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        T te = (T) worldIn.getTileEntity(pos); // 必ずできると信じる
        if (te == null || !(placer instanceof EntityPlayer)) {
            // もし間に合わなかったら、もしくはプレイヤーじゃない別の何かによって置かれたら
            return;
        }
        // 角度情報を入れる
        EntityPlayer ep = (EntityPlayer) placer;
        double angle = GMathHelper.normalizeAngle(-ep.getPitchYaw().y + 180);
        if (ep.isSneaking()) {
            // スニークしながら設置したら90度単位にスナップする
            angle = Math.round(angle / 90f) * 90f;
        }

        te.setAngle(angle); // プレイヤーと逆向きに配置
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // 持っているアイテムがアームだったら無視する
        if (playerIn.getHeldItem(hand).isItemEqual(new ItemStack(GTSItems.TRAFFIC_ARM))) return false;
        return true;
    }
}
