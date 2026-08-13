package com.gfactory.gts.minecraft.client;

import com.gfactory.gts.pack.GTSPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

/**
 * サーバー側でサウンドを停止する命令を送付することができず、マルチプレイの場合にサウンドをクライアントに任せてしまうと
 * サーバー起動時にNoClassで落ちるので、苦肉の策としてクライアント専用のサウンドマネージャーを用意する。
 * これ自体はクライアントでしか呼ばれず、このマネージャーは「どの位置で」「何を鳴らすか」を保持する。
 */
@SideOnly(Side.CLIENT)
public class GTSClientSoundManager {

    private static final HashMap<BlockPos, PositionedSoundRecord> SOUNDS = new HashMap<>();

    /**
     * 指定した座標のTileEntityにおけるサウンド状態をアップデートする。
     * クライアントに同期された瞬間TileEntityから呼ばれる。このクライアント内でサウンドを作成し、それを
     * 入れ込む。path=nullが指定された場合は停止とみなし、再生を終了する。
     * @param pos 座標。TileEntityに紐づく座標を渡す。
     * @param pack 読み込むべき音声が格納されたパック。
     * @param path 読み込むべき音声のパス。キーではなくパスであることに注意。
     */
    public void updateState(BlockPos pos, GTSPack pack, String path) {

        System.out.println("pos="+pos+", pack="+pack+", path="+path);

        if (path == null) {
            // 停止の場合
            this.stopSound(pos);
            SOUNDS.remove(pos);
        }
        else {
            // パスがNULLではない場合、通常はその音源の再生を開始する場合である
            SoundEvent event = pack.getSoundEvents().get(path);
            ISound nowSound = SOUNDS.get(pos);
            if (event == null || (nowSound != null && event.getSoundName().equals(nowSound.getSoundLocation()))) {
                // そんなサウンドイベントは存在しないか既に流れているものと同じなので処理しない
                return;
            }
            // 新しいサウンドイベントを作成
            PositionedSoundRecord sound = new PositionedSoundRecord(
                    event.getSoundName(),
                    SoundCategory.BLOCKS,
                    2.0F, // 音量
                    1.0F, // ピッチ
                    true, // リピート
                    0, // リピートする場合の遅延？
                    ISound.AttenuationType.LINEAR, // 減衰強度
                    pos.getX() + 0.5f,
                    pos.getY() + 0.5f,
                    pos.getZ() + 0.5f
            );
            // 再生開始
            Minecraft.getMinecraft().getSoundHandler().playSound(sound);
            SOUNDS.put(pos, sound);
        }
    }

    /**
     * 音源を停止する。
     * @param pos 座標
     */
    private void stopSound(BlockPos pos) {
        ISound sound = SOUNDS.get(pos);
        if (sound != null) {
            Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
        }
    }
}
