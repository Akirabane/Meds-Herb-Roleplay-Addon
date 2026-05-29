package com.mhafflictions.events;

import com.mhafflictions.MHAfflictions;
import com.mhafflictions.registration.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MHAfflictions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InjuryEventHandler {

    private static final Map<UUID, Integer> waterTicks = new HashMap<>();

    private static final Set<Item> RAW_MEATS = Set.of(
        Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON,
        Items.RABBIT, Items.COD, Items.SALMON, Items.ROTTEN_FLESH
    );

    // ── Chute ─────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float dist = event.getDistance();

        if (dist > 10.0f) {
            apply(player, ModEffects.BROKEN_BONE, 24000, 0);
            apply(player, ModEffects.BLEEDING, 2400, 0);
        } else if (dist > 6.0f) {
            if (player.getRandom().nextFloat() < 0.60f) {
                apply(player, ModEffects.BROKEN_BONE, 24000, 0);
            }
        }
    }

    // ── Combat ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource source = event.getSource();
        float amount = event.getAmount();

        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            applyIfAbsent(player, ModEffects.BURNS, 2400, 0);
            return;
        }

        if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            if (roll(player, 0.50f)) apply(player, ModEffects.BLEEDING, 1200, 0);
            return;
        }

        if (source.getDirectEntity() instanceof net.minecraft.world.entity.LivingEntity) {
            if (amount >= 6.0f && roll(player, 0.35f)) {
                apply(player, ModEffects.LACERATION, 1200, 0);
            } else if (amount >= 3.0f && roll(player, 0.25f)) {
                apply(player, ModEffects.BLEEDING, 800, 0);
            }
        }
    }

    // ── Eau stagnante ─────────────────────────────────────────────────────────
    // Conditions : zone ≥ 3×3×3 d'eau + 60 s minimum dans la zone.
    // Chance : 5 % à 60 s, +5 % toutes les 30 s supplémentaires (cap 50 %).

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (player.isInWater() && isLargeWaterBody(player)) {
            int ticks = waterTicks.getOrDefault(player.getUUID(), 0) + 1;
            waterTicks.put(player.getUUID(), ticks);

            // Première vérification à 60 s (1200 ticks), puis toutes les 30 s (600 ticks)
            if (ticks >= 1200 && (ticks - 1200) % 600 == 0) {
                int extraIntervals = (ticks - 1200) / 600; // 0 à 60s, 1 à 90s, 2 à 120s…
                float chance = Math.min(0.05f + extraIntervals * 0.05f, 0.50f);
                if (roll(player, chance)) {
                    apply(player, ModEffects.BACTERIAL_INFECTION, 12000, 0);
                }
            }
        } else {
            waterTicks.remove(player.getUUID());
        }
    }

    // Vérifie qu'au moins 18/27 blocs du cube 3×3×3 centré sur le joueur sont de l'eau.
    private static boolean isLargeWaterBody(ServerPlayer player) {
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (level.getFluidState(origin.offset(dx, dy, dz)).is(FluidTags.WATER)) {
                        count++;
                    }
                }
            }
        }
        return count >= 18;
    }

    // ── Viande crue ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!RAW_MEATS.contains(event.getItem().getItem())) return;
        if (roll(player, 0.15f)) apply(player, ModEffects.PARASITES, 12000, 0);
    }

    // ── Nettoyage ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        waterTicks.remove(event.getEntity().getUUID());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static void apply(ServerPlayer player, RegistryObject<MobEffect> effect, int duration, int amp) {
        player.addEffect(new MobEffectInstance(effect.get(), duration, amp));
    }

    public static void applyIfAbsent(ServerPlayer player, RegistryObject<MobEffect> effect, int duration, int amp) {
        if (!player.hasEffect(effect.get())) {
            player.addEffect(new MobEffectInstance(effect.get(), duration, amp));
        }
    }

    public static boolean roll(ServerPlayer player, float probability) {
        return player.getRandom().nextFloat() < probability;
    }
}
