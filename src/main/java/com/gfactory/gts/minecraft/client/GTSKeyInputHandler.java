package com.gfactory.gts.minecraft.client;

import com.gfactory.gts.common.GTSI18n;
import com.gfactory.gts.common.GTSPackLoader;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GTSの各キーが押された場合に呼び出されるメソッドをここで定義する
 * クライアント専用！
 */
public class GTSKeyInputHandler {

    /**
     * 作業中かどうかのフラグ（連打防止）
     */
    public boolean progress = false;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (GTSKeyBindings.KEY_RELOAD.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.world == null || mc.player == null || mc.currentScreen != null) {
                // そもそもワールドに入っていないのに呼ばれた場合や、GUIを開いているときなどに呼ばれた場合は無視
                return;
            }

            if (this.progress) {
                // ビジー！
                GTS.LOGGER.warn(new TextComponentTranslation("gts.message.chat.reload_busy"));
                mc.player.sendMessage(new TextComponentTranslation("gts.message.chat.reload_busy"));
                return;
            }


            this.progress = true;

            // リロード用の新しいインスタンスを作成
            GTSPackLoader newInstance = new GTSPackLoader();

            // 別スレッドで監視し、終了時のコールバックを記載する
            CompletableFuture.runAsync(() -> newInstance.reloadPacks(GTS.MOD_DIRECTORY, mc.player, mc.world.isRemote)).thenRun(() -> {
                mc.addScheduledTask(() -> GTS.proxy.registerResourcePack(newInstance.getPacks()));
                // 現在のインスタンスと各パックを比較し、足りないパックがないかどうかをチェックする
                GTSPackLoader original = GTS.LOADER;
                List<String> missingPacks = new ArrayList<>();
                for (GTSPack pack: original.getPacks()) {
                    // オリジナルのパックがない場合は警告GUIを出す
                    if (!newInstance.getPacks().contains(pack)) {
                        GTS.LOGGER.warn(new TextComponentTranslation("gts.message.chat.reload_missing_pack", pack.getName()));
                        missingPacks.add(pack.getName());
                    }
                }
                if (!missingPacks.isEmpty()) {
                    mc.addScheduledTask(() -> {
                        mc.displayGuiScreen(new GuiYesNo(
                                (result, id) -> {
                                    if (result) {
                                        // はいを選ばれた場合
                                        GTS.LOADER = newInstance;
                                    }
                                    else {
                                        mc.player.sendMessage(new TextComponentTranslation("gts.message.chat.reload_cancel"));
                                    }
                                    this.progress = false;
                                    mc.displayGuiScreen(null);
                                },
                                GTSI18n.i18n("gts.message.chat.reload_missing_pack_title"),
                                GTSI18n.i18n("gts.message.chat.reload_missing_pack", String.join(", ", missingPacks)),
                                0
                        ));
                    });
                }
                else {
                    GTS.LOADER = newInstance;
                    this.progress = false;
                }
            });
        }
    }
}
