package com.gfactory.gts.minecraft.command;

import com.gfactory.gts.common.GTSPackLoader;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.pack.GTSPack;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GTSのサーバー向けに使用できるコマンド一覧を登録する。全てのコマンドは/gts なんたらでアクセスできる。
 */
public class GTSCommand extends CommandBase {

    /**
     * スレッド実行中
     */
    private boolean progress = false;

    /**
     * 不足パックが見つかった場合、そのパックをリスト化した保留中のもの
     */
    private List<String> pendingMissingPacks;

    /**
     * 保留中のローダー
     */
    private GTSPackLoader pendingLoader;

    /**
     * リロードの不足パック読み込み確認待ちフラグ
     */
    private boolean pending;

    @Override
    public String getName() {
        return "gts";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/gts reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (progress) return;
        if (args.length >= 1) {
            if (args[0].equals("reload")) {
                if (this.pending) {
                    sender.sendMessage(new TextComponentTranslation("gts.message.chat.reload_confirm_hint"));
                    return;
                }
                // パックのリロードを行う
                this.progress = true;
                GTSPackLoader newInstance = new GTSPackLoader();

                GTS.proxy.registerResourcePack(newInstance.getPacks());
                CompletableFuture.runAsync(() -> newInstance.reloadPacks(GTS.MOD_DIRECTORY, sender, false)).thenRun(() -> {
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

                        sender.sendMessage(
                                new TextComponentTranslation(
                                        "gts.message.chat.reload_missing_pack",
                                        String.join(", ", missingPacks)
                                )
                        );

                        sender.sendMessage(
                                new TextComponentTranslation(
                                        "gts.message.chat.reload_confirm_hint"
                                )
                        );

                        this.pendingLoader = newInstance;
                        this.pendingMissingPacks = missingPacks;
                        this.pending = true;
                        this.progress = false;

                        return;
                    }
                    else {
                        GTS.LOADER = newInstance;
                        this.progress = false;
                    }
                });
            }
            else if (args[0].equals("confirm")) {
                // リロードの不足パックを強制再読み込みする
                if (!this.pending) {
                    sender.sendMessage(new TextComponentString("No reload task"));
                    return;
                }
                GTS.LOADER = this.pendingLoader;

                sender.sendMessage(new TextComponentTranslation("gts.message.chat.reload_comptete"));

                this.pendingLoader = null;
                this.pending = false;
                this.pendingMissingPacks = new ArrayList<>();
                this.progress = false;
            }
            else if (args[0].equals("cancel")) {

                this.pendingLoader = null;
                this.pendingMissingPacks = null;
                this.pending = false;
                this.progress = false;

                sender.sendMessage(new TextComponentTranslation("gts.message.chat.reload_cancel"));
            }
        }
    }
}
