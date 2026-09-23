package com.itemsucker;

import com.itemsucker.command.ItemSuckerCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSuckerMod implements ClientModInitializer {
    public static final String MOD_ID = "itemsucker";
    public static final Logger LOGGER = LoggerFactory.getLogger("ItemSucker");

    private static ItemSuckerConfig config;
    private static ItemSuckerManager manager;

    @Override
    public void onInitializeClient() {
        config = ItemSuckerConfig.load();
        manager = new ItemSuckerManager(config);

        ItemSuckerCommand.register();
        ItemSuckerKeybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                manager.tick();
            }
        });

        LOGGER.info("ItemSucker loaded. Use /itemsucker to configure it.");
    }

    public static ItemSuckerConfig getConfig() {
        return config;
    }

    public static ItemSuckerManager getManager() {
        return manager;
    }

    public static void setEnabled(boolean enabled) {
        config.enabled = enabled;
        manager.onToggle(enabled);
        config.save();
    }
}
