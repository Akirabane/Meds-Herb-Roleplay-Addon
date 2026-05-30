package com.mhafflictions.events;

import com.mhafflictions.MHAfflictions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Fragile ice over water. Standing still on a {@code minecraft:ice} block that sits
 * on top of water cracks it after a delay that shrinks with water depth. Each ice
 * block runs its own timer; leaving the block resets it. After breaking, the ice
 * regenerates at the exact same spot one second later — possibly trapping the player.
 *
 * Server-side only. A single server tick collects every occupied ice block, so
 * resets, multi-player occupancy and per-block timers are handled consistently.
 */
@Mod.EventBusSubscriber(modid = MHAfflictions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FragileIceHandler {

    private static final int REGEN_DELAY_TICKS = 20; // 1 second
    private static final int MAX_DEPTH = 5;          // 5+ is instant, no need to count further

    // Accumulated standing time per ice block.
    private static final Map<GlobalPos, Integer> standTimers = new HashMap<>();
    // Remaining ticks before an already-broken ice block regenerates.
    private static final Map<GlobalPos, Integer> regenTimers = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;

        // 1. Find every fragile-ice block a player is standing on this tick (pos → water depth).
        Map<GlobalPos, Integer> occupied = collectOccupied(server);

        // 2. Drop timers for blocks no longer occupied (player left → reset to zero).
        standTimers.keySet().retainAll(occupied.keySet());

        // 3. Advance occupied timers; collect blocks that reached their break threshold.
        List<GlobalPos> toBreak = new ArrayList<>();
        for (Map.Entry<GlobalPos, Integer> e : occupied.entrySet()) {
            GlobalPos pos = e.getKey();
            int delay = breakDelay(e.getValue());
            int t = standTimers.getOrDefault(pos, 0) + 1;
            if (t >= delay) {
                toBreak.add(pos);
            } else {
                standTimers.put(pos, t);
            }
        }

        // 4. Break the ice (no drops) and schedule its regeneration.
        for (GlobalPos pos : toBreak) {
            standTimers.remove(pos);
            ServerLevel level = server.getLevel(pos.dimension());
            if (level == null) continue;
            if (level.getBlockState(pos.pos()).is(Blocks.ICE)) {
                level.destroyBlock(pos.pos(), false);
                regenTimers.put(pos, REGEN_DELAY_TICKS);
            }
        }

        // 5. Tick regeneration timers and restore ice when due.
        if (!regenTimers.isEmpty()) tickRegen(server);
    }

    private static Map<GlobalPos, Integer> collectOccupied(MinecraftServer server) {
        Map<GlobalPos, Integer> occupied = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || !player.onGround()) continue;

                BlockPos icePos = player.blockPosition().below();
                if (!level.getBlockState(icePos).is(Blocks.ICE)) continue;

                int depth = waterDepth(level, icePos);
                if (depth <= 0) continue; // solid block under the ice → safe

                // Several players on the same block share one timer; keep the deepest reading.
                occupied.merge(GlobalPos.of(level.dimension(), icePos), depth, Math::max);
            }
        }
        return occupied;
    }

    private static void tickRegen(MinecraftServer server) {
        Iterator<Map.Entry<GlobalPos, Integer>> it = regenTimers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GlobalPos, Integer> e = it.next();
            int t = e.getValue() - 1;
            if (t > 0) {
                e.setValue(t);
                continue;
            }
            it.remove();
            GlobalPos pos = e.getKey();
            ServerLevel level = server.getLevel(pos.dimension());
            if (level == null) continue;
            // Restore ice only over empty space or water, so we never overwrite a
            // block a player may have placed in the hole meanwhile.
            BlockState current = level.getBlockState(pos.pos());
            if (current.isAir() || !current.getFluidState().isEmpty()) {
                level.setBlockAndUpdate(pos.pos(), Blocks.ICE.defaultBlockState());
            }
        }
    }

    /** Counts continuous water blocks directly beneath the ice (capped at MAX_DEPTH). */
    private static int waterDepth(ServerLevel level, BlockPos icePos) {
        int depth = 0;
        BlockPos.MutableBlockPos p = icePos.below().mutable();
        while (depth < MAX_DEPTH && level.getFluidState(p).is(FluidTags.WATER)) {
            depth++;
            p.move(0, -1, 0);
        }
        return depth;
    }

    /** Break delay in ticks by water depth. */
    private static int breakDelay(int depth) {
        return switch (depth) {
            case 1 -> 1200; // 60 s
            case 2 -> 600;  // 30 s
            case 3 -> 100;  // 5 s
            case 4 -> 20;   // 1 s
            default -> 0;   // 5+ → instant
        };
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        standTimers.clear();
        regenTimers.clear();
    }
}
