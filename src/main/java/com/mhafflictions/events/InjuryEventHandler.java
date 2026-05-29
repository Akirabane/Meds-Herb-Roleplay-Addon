package com.mhafflictions.events;

import com.mhafflictions.MHAfflictions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MHAfflictions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InjuryEventHandler {

    // Tracks consecutive ticks a player has been in water
    private static final Map<UUID, Integer> waterTicks = new HashMap<>();

    // Raw foods that can carry parasites
    private static final Set<Item> RAW_MEATS = Set.of(
        Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON,
        Items.RABBIT, Items.COD, Items.SALMON, Items.ROTTEN_FLESH
    );

    // ---------- CHUTE ----------

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float dist = event.getDistance();

        if (dist > 10.0f) {
            // Grosse chute : os cassé + saignement garantis
            applyEffect(player, "broken_bone", 24000, 0);
            applyEffect(player, "bleeding", 2400, 0);
        } else if (dist > 6.0f) {
            // Chute moyenne : 60 % de chance d'os cassé
            if (chance(player, 0.60f)) {
                applyEffect(player, "broken_bone", 24000, 0);
            }
        }
    }

    // ---------- COMBAT ----------

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource source = event.getSource();
        float amount = event.getAmount();

        // Feu / lave → Brûlures (n'empile pas si déjà présent)
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            applyIfAbsent(player, "burns", 2400, 0);
            return;
        }

        // Projectile (flèche, trident…) → Saignement
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            if (chance(player, 0.50f)) applyEffect(player, "bleeding", 1200, 0);
            return;
        }

        // Corps à corps
        if (source.getDirectEntity() instanceof net.minecraft.world.entity.LivingEntity) {
            if (amount >= 6.0f && chance(player, 0.35f)) {
                // Coup puissant → Lacération
                applyEffect(player, "laceration", 1200, 0);
            } else if (amount >= 3.0f && chance(player, 0.25f)) {
                // Coup normal → Saignement
                applyEffect(player, "bleeding", 800, 0);
            }
        }
    }

    // ---------- EAU STAGNANTE ----------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (player.isInWater()) {
            int ticks = waterTicks.getOrDefault(player.getUUID(), 0) + 1;
            waterTicks.put(player.getUUID(), ticks);

            // Toutes les minutes passées dans l'eau : 5 % de chance d'infection bactérienne
            if (ticks % 1200 == 0 && chance(player, 0.05f)) {
                applyEffect(player, "bacterial_infection", 12000, 0);
            }
        } else {
            waterTicks.remove(player.getUUID());
        }
    }

    // ---------- NOURRITURE CRU ----------

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!RAW_MEATS.contains(event.getItem().getItem())) return;

        // Viande crue → 15 % de chance de parasites
        if (chance(player, 0.15f)) {
            applyEffect(player, "parasites", 12000, 0);
        }
    }

    // ---------- NETTOYAGE ----------

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        waterTicks.remove(event.getEntity().getUUID());
    }

    // ---------- HELPERS ----------

    private static void applyEffect(ServerPlayer player, String name, int duration, int amplifier) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("meds_and_herbs", name));
        if (effect == null) {
            MHAfflictions.LOGGER.warn("[MHAfflictions] Effect meds_and_herbs:{} not found — is Meds And Herbs loaded?", name);
            return;
        }
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    private static void applyIfAbsent(ServerPlayer player, String name, int duration, int amplifier) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("meds_and_herbs", name));
        if (effect != null && !player.hasEffect(effect)) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private static boolean chance(Player player, float probability) {
        return player.getRandom().nextFloat() < probability;
    }
}
