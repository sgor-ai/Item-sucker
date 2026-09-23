package com.itemsucker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.gson.JsonParseException;

/**
 * Holds every user-configurable option and persists them to
 * config/itemsucker.json so settings survive a restart.
 */
public class ItemSuckerConfig {
    public enum Mode { WHITELIST, BLACKLIST }
    public enum MoveMode { TELEPORT, BARITONE }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("itemsucker.json");

    public boolean enabled = false;

    public boolean onlyOnGround = true;
    public boolean onlyPickupable = true;
    public boolean checkCollisions = true;

    public boolean teleportBack = true;
    public int waitTicks = 10;
    public boolean showPickupNotifications = true;

    // Baritone move mode only
    public int pathRange = 0;
    public boolean returnToOrigin = true;
    public int returnRange = 0;

    public MoveMode moveMode = BaritoneBridge.IS_AVAILABLE ? MoveMode.BARITONE : MoveMode.TELEPORT;

    public Mode mode = Mode.BLACKLIST;

    // Stored as registry ids (e.g. "minecraft:diamond") so they survive
    // being written to disk.
    public Set<String> whitelist = new LinkedHashSet<>();
    public Set<String> blacklist = new LinkedHashSet<>();

    private static final class Data {
        boolean enabled;
        boolean onlyOnGround;
        boolean onlyPickupable;
        boolean checkCollisions;
        boolean teleportBack;
        int waitTicks;
        Boolean showPickupNotifications;
        int pathRange;
        boolean returnToOrigin;
        int returnRange;
        MoveMode moveMode;
        Mode mode;
        Set<String> whitelist;
        Set<String> blacklist;
    }

    public boolean isReturnToOrigin() {
        return returnToOrigin;
    }

    public void setReturnToOrigin(boolean returnToOrigin) {
        this.returnToOrigin = returnToOrigin;
    }

    public boolean isAllowed(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        return mode == Mode.WHITELIST ? whitelist.contains(id) : !blacklist.contains(id);
    }

    public void toggleItem(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        Set<String> list = mode == Mode.WHITELIST ? whitelist : blacklist;
        if (!list.remove(id)) list.add(id);
    }

    public boolean isSelected(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        Set<String> list = mode == Mode.WHITELIST ? whitelist : blacklist;
        return list.contains(id);
    }

    public static ItemSuckerConfig load() {
        if (!Files.exists(PATH)) {
            ItemSuckerConfig fresh = new ItemSuckerConfig();
            fresh.save();
            return fresh;
        }

        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            ItemSuckerConfig cfg = new ItemSuckerConfig();
            if (data != null) {
                cfg.enabled = data.enabled;
                cfg.onlyOnGround = data.onlyOnGround;
                cfg.onlyPickupable = data.onlyPickupable;
                cfg.checkCollisions = data.checkCollisions;
                cfg.teleportBack = data.teleportBack;
                cfg.waitTicks = data.waitTicks == 0 ? 10 : data.waitTicks;
                if (data.showPickupNotifications != null) {
                    cfg.showPickupNotifications = data.showPickupNotifications;
                }
                cfg.pathRange = data.pathRange;
                cfg.returnToOrigin = data.returnToOrigin;
                cfg.returnRange = data.returnRange;
                if (data.moveMode != null) cfg.moveMode = data.moveMode;
                if (data.mode != null) cfg.mode = data.mode;
                if (data.whitelist != null) cfg.whitelist = data.whitelist;
                if (data.blacklist != null) cfg.blacklist = data.blacklist;
            }
            cfg.sanitize();
            return cfg;
        } catch (IOException | JsonParseException e) {
            ItemSuckerMod.LOGGER.error("Could not read itemsucker.json; using safe defaults", e);
            return new ItemSuckerConfig();
        }
    }

    public void sanitize() {
        waitTicks = Math.max(0, Math.min(200, waitTicks));
        pathRange = Math.max(0, Math.min(16, pathRange));
        returnRange = Math.max(0, Math.min(16, returnRange));
        if (moveMode == null) moveMode = BaritoneBridge.IS_AVAILABLE ? MoveMode.BARITONE : MoveMode.TELEPORT;
        if (mode == null) mode = Mode.BLACKLIST;
        if (whitelist == null) whitelist = new LinkedHashSet<>();
        if (blacklist == null) blacklist = new LinkedHashSet<>();
    }

    public void save() {
        Data data = new Data();
        data.enabled = enabled;
        data.onlyOnGround = onlyOnGround;
        data.onlyPickupable = onlyPickupable;
        data.checkCollisions = checkCollisions;
        data.teleportBack = teleportBack;
        data.waitTicks = waitTicks;
        data.showPickupNotifications = showPickupNotifications;
        data.pathRange = pathRange;
        data.returnToOrigin = returnToOrigin;
        data.returnRange = returnRange;
        data.moveMode = moveMode;
        data.mode = mode;
        data.whitelist = whitelist;
        data.blacklist = blacklist;

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            ItemSuckerMod.LOGGER.error("Could not save itemsucker.json", e);
        }
    }
}
