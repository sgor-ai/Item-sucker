package com.itemsucker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Standalone re-implementation of Meteorist's "ItemSucker" module.
 *
 * Two move modes, same as the original:
 *  - TELEPORT: instantly snaps the player onto the nearest matching item.
 *  - BARITONE: if you have Baritone installed, paths the player over to
 *    the items like a normal walk, and returns to the starting spot when
 *    done. This is the mode that "walks towards" the item instead of
 *    snapping to it.
 *
 * IMPORTANT: Teleport mode moves the player instantly. Baritone mode
 * walks normally (via pathfinding) so it's much less likely to trip
 * server-side anti-cheat, but it still automates movement, which some
 * servers don't allow either. Use responsibly.
 */
public class ItemSuckerManager {
    private static final double COLLECTION_RANGE = 64.0;
    private final ItemSuckerConfig config;
    private final BaritoneBridge baritone = new BaritoneBridge();

    private int timer = 0;
    private Vec3 startPos = null;
    private final Map<Integer, ItemStack> watching = new HashMap<>();
    private final Map<Item, Integer> sessionTally = new LinkedHashMap<>();
    private boolean wasCollecting = false;
    private String lastBaritoneGoal = "";
    private int baritoneRefreshTicks = 0;
    private int lastTeleportTargetId = Integer.MIN_VALUE;

    public ItemSuckerManager(ItemSuckerConfig config) {
        this.config = config;
    }

    public void onToggle(boolean nowEnabled) {
        timer = 0;
        startPos = null;
        baritone.cancelEverything();
        watching.clear();
        sessionTally.clear();
        wasCollecting = false;
        lastBaritoneGoal = "";
        baritoneRefreshTicks = 0;
        lastTeleportTargetId = Integer.MIN_VALUE;
    }

    public void tick() {
        if (!config.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        List<ItemEntity> currentMatches = findAllTargets(mc);
        updatePickupTracking(mc, currentMatches);

        if (config.moveMode == ItemSuckerConfig.MoveMode.BARITONE) {
            tickBaritone(mc, currentMatches);
        } else {
            tickTeleport(mc, player, currentMatches);
        }
    }

    private void updatePickupTracking(Minecraft mc, List<ItemEntity> currentMatches) {
        for (ItemEntity entity : currentMatches) {
            watching.putIfAbsent(entity.getId(), entity.getItem().copy());
        }
        Iterator<Map.Entry<Integer, ItemStack>> iterator = watching.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ItemStack> entry = iterator.next();
            Entity entity = mc.level.getEntity(entry.getKey());
            if (entity == null || entity.isRemoved()) {
                sessionTally.merge(entry.getValue().getItem(), entry.getValue().getCount(), Integer::sum);
                iterator.remove();
            }
        }
        boolean collecting = !currentMatches.isEmpty() || !watching.isEmpty();
        if (wasCollecting && !collecting) {
            flushSessionSummary(mc);
        }
        wasCollecting = collecting;
    }

    private void flushSessionSummary(Minecraft mc) {
        if (sessionTally.isEmpty() || !config.showPickupNotifications) {
            sessionTally.clear();
            return;
        }
        StringBuilder text = new StringBuilder("§b[ItemSucker] §fCollection complete: §a");
        boolean first = true;
        for (Map.Entry<Item, Integer> entry : sessionTally.entrySet()) {
            if (!first) text.append("§f, §a");
            text.append(entry.getValue()).append("x ")
                    .append(new ItemStack((ItemLike) entry.getKey()).getDisplayName().getString());
            first = false;
        }
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(text.toString()));
        }
        sessionTally.clear();
    }

    private void tickTeleport(Minecraft mc, LocalPlayer player, List<ItemEntity> currentMatches) {
        if (timer > 0) timer -= 1;

        ItemEntity target = currentMatches.isEmpty() ? null : currentMatches.get(0);

        if (target != null) {
            if (config.teleportBack) {
                startPos = startPos == null ? player.position() : startPos;
                timer = config.waitTicks;
            }
            if (target.getId() != lastTeleportTargetId) {
                player.setPos(target.getX(), target.getY(), target.getZ());
                lastTeleportTargetId = target.getId();
            }
        } else if (timer <= 0 && config.teleportBack && startPos != null) {
            player.setPos(startPos.x, startPos.y, startPos.z);
            startPos = null;
            lastTeleportTargetId = Integer.MIN_VALUE;
        } 
    }

    private void tickBaritone(Minecraft mc, List<ItemEntity> targets) {
        if (!BaritoneBridge.IS_AVAILABLE) {
            // Baritone isn't actually loaded - fall back so the module
            // still does *something* instead of silently doing nothing.
            config.moveMode = ItemSuckerConfig.MoveMode.TELEPORT;
            config.save();
            tickTeleport(mc, mc.player, targets);
            return;
        }

        if (!targets.isEmpty()) {
            if (baritoneRefreshTicks > 0) baritoneRefreshTicks--;
            String goal = targets.stream()
                    .map(entity -> Integer.toString(entity.getId()))
                    .sorted()
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
            if (!goal.equals(lastBaritoneGoal) || baritoneRefreshTicks == 0) {
                baritone.setGoalNear(targets, config.pathRange);
                lastBaritoneGoal = goal;
                baritoneRefreshTicks = 10;
            }
            if (config.returnToOrigin && startPos == null) {
                startPos = Vec3.atCenterOf(mc.player.blockPosition());
            }
        } else if (config.returnToOrigin && startPos != null) {
            lastBaritoneGoal = "";
            baritone.gotoBlock(BlockPos.containing(startPos), config.returnRange);
            startPos = null;
        } 
    }

    /** Used by the render layer / Baritone mode to gather every item currently matching the filter. */
    public List<ItemEntity> findAllTargets(Minecraft mc) {
        List<ItemEntity> result = new ArrayList<>();
        ClientLevel level = mc.level;
        if (level == null) return result;

        AABB searchBox = mc.player.getBoundingBox().inflate(COLLECTION_RANGE);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (matches(mc, itemEntity)) result.add(itemEntity);
        }
        result.sort(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
        return result;
    }

    private ItemEntity findNearestTarget(Minecraft mc) {
        ItemEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            if (!matches(mc, itemEntity)) continue;

            double dist = itemEntity.distanceToSqr(mc.player);
            if (dist < bestDist) {
                bestDist = dist;
                best = itemEntity;
            }
        }

        return best;
    }

    private boolean matches(Minecraft mc, ItemEntity itemEntity) {
        if (config.onlyPickupable && itemEntity.hasPickUpDelay()) return false;
        if (config.onlyOnGround && !itemEntity.onGround()) return false;
        if (mc.player.distanceTo(itemEntity) > COLLECTION_RANGE) return false;
        if (!config.isAllowed(itemEntity.getItem().getItem())) return false;
        if (config.moveMode == ItemSuckerConfig.MoveMode.TELEPORT
                && config.checkCollisions && !canTeleportTo(mc, itemEntity.position())) return false;
        return true;
    }

    private boolean canTeleportTo(Minecraft mc, Vec3 pos) {
        AABB box = boundingBoxAt(mc, pos);

        Iterable<VoxelShape> collisions = mc.level.getBlockCollisions(mc.player, box);
        List<VoxelShape> collisionList = StreamSupport.stream(collisions.spliterator(), false)
                .filter(shape -> !shape.isEmpty())
                .toList();

        return collisionList.isEmpty();
    }

    private AABB boundingBoxAt(Minecraft mc, Vec3 pos) {
        Vec3 offset = pos.subtract(mc.player.getBoundingBox().getBottomCenter());
        return mc.player.getBoundingBox().move(offset.x, offset.y, offset.z);
    }
}
