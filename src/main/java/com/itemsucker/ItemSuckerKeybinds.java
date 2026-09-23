package com.itemsucker;

import com.itemsucker.gui.ItemSelectionScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ItemSuckerKeybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(ItemSuckerMod.MOD_ID, "itemsucker"));
    private static KeyMapping toggleKey;
    private static KeyMapping guiKey;

    private ItemSuckerKeybinds() {
    }

    public static void register() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.itemsucker.toggle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));
        guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.itemsucker.gui", InputConstants.Type.KEYSYM, 77, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                if (client.player != null) {
                    ItemSuckerConfig config = ItemSuckerMod.getConfig();
                    ItemSuckerMod.setEnabled(!config.enabled);
                    client.player.sendSystemMessage(Component.literal(config.enabled
                            ? "§b[ItemSucker] §aEnabled"
                            : "§b[ItemSucker] §cDisabled"));
                }
            }
            while (guiKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.setScreenAndShow(new ItemSelectionScreen(null));
                }
            }
        });
    }
}
