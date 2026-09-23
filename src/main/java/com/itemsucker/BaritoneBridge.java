package com.itemsucker;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.pathing.goals.GoalNear;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * Thin wrapper around Baritone's API, ported from Meteorist's
 * MeteoristBaritoneUtils. Kept as compileOnly so this mod still builds
 * and runs fine even when Baritone isn't installed - IS_AVAILABLE is
 * checked before ever touching a Baritone class.
 */
public class BaritoneBridge {
    // The Fabric mod id in the jar you have is "baritone-meteor", not
    // plain "baritone" - matched against your uploaded
    // baritone-meteor-26_2.jar (fabric.mod.json -> "id": "baritone-meteor").
    public static final boolean IS_AVAILABLE = FabricLoader.getInstance().isModLoaded("baritone-meteor");

    private final IBaritone baritone;

    public BaritoneBridge() {
        this.baritone = IS_AVAILABLE ? BaritoneAPI.getProvider().getPrimaryBaritone() : null;
    }

    public void cancelEverything() {
        if (IS_AVAILABLE) baritone.getPathingBehavior().cancelEverything();
    }

    public boolean isPathing() {
        return IS_AVAILABLE && baritone.getPathingBehavior().isPathing();
    }

    /** Paths towards the nearest of the given entities, within `range` blocks of it. */
    public void setGoalNear(List<? extends Entity> entities, int range) {
        if (!IS_AVAILABLE || entities.isEmpty()) return;

        Goal[] goals = entities.stream()
                .map(entity -> (Goal) new GoalNear(entity.blockPosition(), range))
                .toArray(Goal[]::new);

        baritone.getCustomGoalProcess().setGoalAndPath(new GoalComposite(goals));
    }

    /** Paths back to a fixed block position (used to return "home" after collecting everything). */
    public void gotoBlock(net.minecraft.core.BlockPos pos, int range) {
        if (!IS_AVAILABLE) return;
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos, range));
    }
}
