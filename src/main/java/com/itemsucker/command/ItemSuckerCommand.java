package com.itemsucker.command;

import com.itemsucker.ItemSuckerConfig;
import com.itemsucker.ItemSuckerMod;
import com.itemsucker.gui.ItemSelectionScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

/**
 * Registers "/itemsucker" as a CLIENT-side only command (works in
 * singleplayer and on any server, since it never touches the server).
 */
public class ItemSuckerCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("itemsucker")
                    .executes(ctx -> {
                        toggle(ctx.getSource());
                        return 1;
                    })
                    .then(literal("on").executes(ctx -> {
                        setEnabled(ctx.getSource(), true);
                        return 1;
                    }))
                    .then(literal("off").executes(ctx -> {
                        setEnabled(ctx.getSource(), false);
                        return 1;
                    }))
                    .then(literal("toggle").executes(ctx -> {
                        toggle(ctx.getSource());
                        return 1;
                    }))
                    .then(literal("gui").executes(ctx -> {
                        Minecraft.getInstance().execute(() ->
                                Minecraft.getInstance().setScreenAndShow(new ItemSelectionScreen(null)));
                        return 1;
                    }))
                    .then(literal("mode").then(literal("whitelist").executes(ctx -> {
                                setMode(ctx.getSource(), ItemSuckerConfig.Mode.WHITELIST);
                                return 1;
                            }))
                            .then(literal("blacklist").executes(ctx -> {
                                setMode(ctx.getSource(), ItemSuckerConfig.Mode.BLACKLIST);
                                return 1;
                            })))
                    .then(literal("move").then(literal("teleport").executes(ctx -> {
                                setMoveMode(ctx.getSource(), ItemSuckerConfig.MoveMode.TELEPORT);
                                return 1;
                            }))
                            .then(literal("baritone").executes(ctx -> {
                                setMoveMode(ctx.getSource(), ItemSuckerConfig.MoveMode.BARITONE);
                                return 1;
                            })))
                    .then(literal("return").executes(ctx -> {
                        toggleReturn(ctx.getSource());
                        return 1;
                    }).then(literal("on").executes(ctx -> {
                        setReturn(ctx.getSource(), true);
                        return 1;
                    })).then(literal("off").executes(ctx -> {
                        setReturn(ctx.getSource(), false);
                        return 1;
                    })).then(literal("toggle").executes(ctx -> {
                        toggleReturn(ctx.getSource());
                        return 1;
                    })))
                    .then(literal("notify").executes(ctx -> {
                        setNotify(ctx.getSource(), !ItemSuckerMod.getConfig().showPickupNotifications);
                        return 1;
                    }).then(literal("on").executes(ctx -> {
                        setNotify(ctx.getSource(), true);
                        return 1;
                    })).then(literal("off").executes(ctx -> {
                        setNotify(ctx.getSource(), false);
                        return 1;
                    })))
            );
        });
    }

    private static void toggle(FabricClientCommandSource source) {
        ItemSuckerConfig cfg = ItemSuckerMod.getConfig();
        setEnabled(source, !cfg.enabled);
    }

    private static void setEnabled(FabricClientCommandSource source, boolean value) {
        ItemSuckerMod.setEnabled(value);
        source.sendFeedback(Component.literal(value
                ? "§b[ItemSucker] §aEnabled"
                : "§b[ItemSucker] §cDisabled"));
    }

    private static void setMode(FabricClientCommandSource source, ItemSuckerConfig.Mode mode) {
        ItemSuckerConfig cfg = ItemSuckerMod.getConfig();
        cfg.mode = mode;
        cfg.save();
        source.sendFeedback(Component.literal("§b[ItemSucker] §fFilter mode set to " + mode.name().toLowerCase()));
    }

    private static void setMoveMode(FabricClientCommandSource source, ItemSuckerConfig.MoveMode moveMode) {
        if (moveMode == ItemSuckerConfig.MoveMode.BARITONE && !com.itemsucker.BaritoneBridge.IS_AVAILABLE) {
            source.sendError(Component.literal("§b[ItemSucker] §cBaritone is not installed; keeping Teleport mode."));
            return;
        }
        ItemSuckerConfig cfg = ItemSuckerMod.getConfig();
        cfg.moveMode = moveMode;
        cfg.save();
        source.sendFeedback(Component.literal("§b[ItemSucker] §fMovement mode set to " + moveMode.name().toLowerCase()));
    }

    private static void toggleReturn(FabricClientCommandSource source) {
        setReturn(source, !ItemSuckerMod.getConfig().isReturnToOrigin());
    }

    private static void setReturn(FabricClientCommandSource source, boolean value) {
        ItemSuckerConfig cfg = ItemSuckerMod.getConfig();
        cfg.setReturnToOrigin(value);
        cfg.save();
        source.sendFeedback(Component.literal(value
                ? "§b[ItemSucker] §fReturn to origin: §aON"
                : "§b[ItemSucker] §fReturn to origin: §cOFF"));
    }

    private static void setNotify(FabricClientCommandSource source, boolean value) {
        ItemSuckerConfig cfg = ItemSuckerMod.getConfig();
        cfg.showPickupNotifications = value;
        cfg.save();
        source.sendFeedback(Component.literal(value
                ? "§b[ItemSucker] §fPickup notifications: §aON"
                : "§b[ItemSucker] §fPickup notifications: §cOFF"));
    }

}
