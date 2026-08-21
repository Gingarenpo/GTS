package com.gfactory.gts.minecraft.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * GTSで使用するキーショートカットの一覧。KeyBindingを作成し、そのキーを実際の処理に割り当てる。
 * クライアント専用。
 */
public class GTSKeyBindings {
    public static KeyBinding KEY_RELOAD;

    public static void init() {
        KEY_RELOAD = new KeyBinding(
                "key.gts.reload",
                Keyboard.KEY_F9,
                "key.categories.gts"
        );
        ClientRegistry.registerKeyBinding(KEY_RELOAD);
    }
}
