package com.mhafflictions.events;

import com.mhafflictions.MHAfflictions;
import com.mhafflictions.registration.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Détecte l'utilisation d'items de Meds And Herbs et applique/retire
 * les effets ÉQUIVALENTS de notre propre système.
 * M&H n'est pas une dépendance obligatoire — si absent, ce handler
 * ne trouvera jamais d'items avec le namespace "meds_and_herbs".
 */
@Mod.EventBusSubscriber(modid = MHAfflictions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MedsHerbsIntegration {

    private static final String MH_NAMESPACE = "meds_and_herbs";

    // ── Tous les items M&H (instant right-click) ──────────────────────────────

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null || !MH_NAMESPACE.equals(rl.getNamespace())) return;

        String path = rl.getPath();
        if (path.startsWith("syringe_")) {
            handleSyringe(player, path);
        } else if (path.contains("dressing")) {
            handleDressing(player, path);
        } else if (path.startsWith("medkit_")) {
            handleMedkit(player, path);
        } else if (path.equals("splint")) {
            player.addEffect(new MobEffectInstance(ModEffects.BONE_HEAL.get(), 3600, 0));
        }
    }

    // ── Fallback pour items avec durée d'utilisation ───────────────────────────

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null || !MH_NAMESPACE.equals(rl.getNamespace())) return;

        String path = rl.getPath();
        if (path.contains("dressing")) {
            handleDressing(player, path);
        } else if (path.startsWith("medkit_")) {
            handleMedkit(player, path);
        } else if (path.equals("splint")) {
            player.addEffect(new MobEffectInstance(ModEffects.BONE_HEAL.get(), 3600, 0));
        }
    }

    // ── Logique Seringues ──────────────────────────────────────────────────────

    private static void handleSyringe(ServerPlayer player, String path) {
        switch (path) {

            case "syringe_herbal" -> handleHerbal(player);

            case "syringe_poison" ->
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 0));

            case "syringe_belladonna_poison" ->
                player.addEffect(new MobEffectInstance(ModEffects.EFFECT_BELLADONNA_BERRY.get(), 600, 3));

            case "syringe_belladonna" -> {
                if (player.hasEffect(ModEffects.MUSHROOM_POISONING.get())) {
                    player.removeEffect(ModEffects.MUSHROOM_POISONING.get());
                } else {
                    player.addEffect(new MobEffectInstance(ModEffects.EFFECT_BELLADONNA_BERRY.get(), 600, 1));
                }
            }

            case "syringe_vinca" -> {
                if (player.hasEffect(ModEffects.INTERNAL_BLEEDING.get())) {
                    player.removeEffect(ModEffects.INTERNAL_BLEEDING.get());
                } else if (player.hasEffect(ModEffects.THROMBOSIS.get())) {
                    player.hurt(player.damageSources().magic(), player.getMaxHealth());
                } else {
                    player.addEffect(new MobEffectInstance(ModEffects.THROMBOSIS.get(), 24000, 0));
                }
            }

            case "syringe_sweet_clover" -> {
                if (player.hasEffect(ModEffects.THROMBOSIS.get())) {
                    player.removeEffect(ModEffects.THROMBOSIS.get());
                } else if (player.hasEffect(ModEffects.INTERNAL_BLEEDING.get())) {
                    player.hurt(player.damageSources().magic(), player.getMaxHealth());
                } else {
                    player.addEffect(new MobEffectInstance(ModEffects.INTERNAL_BLEEDING.get(), 1200, 0));
                }
            }

            case "syringe_artemisia" -> {
                if (player.hasEffect(ModEffects.PARASITES.get())) {
                    player.removeEffect(ModEffects.PARASITES.get());
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
                }
            }

            case "syringe_chamomile" -> {
                boolean cured = false;
                for (var vanilla : List.of(MobEffects.CONFUSION, MobEffects.HUNGER,
                                           MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN)) {
                    if (player.hasEffect(vanilla)) { player.removeEffect(vanilla); cured = true; }
                }
                if (!cured) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0));
            }

            case "syringe_opium"    -> handleOpium(player, 300, 0.25f);
            case "syringe_morphine" -> handleOpium(player, 600, 0.10f);

            case "syringe_antidote" -> player.removeEffect(MobEffects.POISON);

            case "syringe_hpa" -> {
                if (player.hasEffect(ModEffects.HPP.get())) {
                    player.removeEffect(ModEffects.HPP.get());
                    player.addEffect(new MobEffectInstance(ModEffects.HPA.get(), 1200, 0));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                }
            }

            case "syringe_hpp" -> {
                if (player.hasEffect(ModEffects.HPA.get())) {
                    player.removeEffect(ModEffects.HPP.get());
                } else {
                    player.addEffect(new MobEffectInstance(ModEffects.HPP.get(), 200, 0));
                }
            }

            case "syringe_adrenaline" -> {
                if (player.hasEffect(ModEffects.ADRENALINE.get())) {
                    player.hurt(player.damageSources().magic(), 10.0f); // arythmie
                } else {
                    player.removeEffect(MobEffects.REGENERATION);
                    player.removeEffect(MobEffects.WEAKNESS);
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    player.addEffect(new MobEffectInstance(ModEffects.ADRENALINE.get(), 1200, 1));
                }
            }

            case "syringe_mushroom" -> {
                if (player.getRandom().nextFloat() < 0.15f) {
                    player.addEffect(new MobEffectInstance(ModEffects.MUSHROOM_POISONING.get(), 2400, 0));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 1));
                }
            }

            case "syringe_caffeine" ->
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));

            case "syringe_glucose" ->
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0));

            case "syringe_blood" -> {
                player.heal(4.0f);
                player.removeEffect(ModEffects.BLOOD_LOSS.get());
            }

            case "syringe_blood_hpp" -> {
                player.heal(4.0f);
                player.removeEffect(ModEffects.BLOOD_LOSS.get());
                player.addEffect(new MobEffectInstance(ModEffects.HPP.get(), 2400, 0));
            }

            case "syringe_blood_poison" -> {
                player.heal(4.0f);
                player.removeEffect(ModEffects.BLOOD_LOSS.get());
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            }

            case "syringe_blood_adrenaline" -> {
                player.heal(4.0f);
                player.removeEffect(ModEffects.BLOOD_LOSS.get());
                if (!player.hasEffect(ModEffects.ADRENALINE.get())) {
                    player.addEffect(new MobEffectInstance(ModEffects.ADRENALINE.get(), 200, 0));
                }
            }

            case "syringe_penicillin" -> {
                if (player.hasEffect(ModEffects.BACTERIAL_INFECTION.get())) {
                    player.removeEffect(ModEffects.BACTERIAL_INFECTION.get());
                    player.addEffect(new MobEffectInstance(ModEffects.ANTIBIOTICS.get(), 2400, 0));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2400, 0));
                }
            }

            case "syringe_ethanol" -> {
                if (player.hasEffect(ModEffects.METHANOL_POISONING.get())) {
                    player.removeEffect(ModEffects.METHANOL_POISONING.get());
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0));
                }
            }

            case "syringe_methanol" -> {
                if (player.hasEffect(ModEffects.METHANOL_POISONING.get())) {
                    player.hurt(player.damageSources().magic(), player.getMaxHealth());
                } else {
                    player.addEffect(new MobEffectInstance(ModEffects.METHANOL_POISONING.get(), 12000, 0));
                }
            }

            default -> {
                // Seringue M&H non reconnue — log pour debug
                MHAfflictions.LOGGER.debug("[MHAfflictions] Unhandled M&H syringe: {}", path);
            }
        }
    }

    // ── Logique Pansements ────────────────────────────────────────────────────

    private static void handleDressing(ServerPlayer player, String path) {
        switch (path) {
            case "dressing_cheap" -> {
                if (player.getRandom().nextFloat() < 0.50f)
                    player.removeEffect(ModEffects.BLEEDING.get());
            }
            case "dressing", "dressing_advanced" ->
                player.removeEffect(ModEffects.BLEEDING.get());

            case "dressing_honey" -> {
                player.removeEffect(ModEffects.BLEEDING.get());
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0));
            }
            case "dressing_alcohol" -> {
                player.removeEffect(ModEffects.BLEEDING.get());
                if (!player.hasEffect(ModEffects.BACTERIAL_INFECTION.get()))
                    player.addEffect(new MobEffectInstance(ModEffects.ANTISEPTIC.get(), 1200, 0));
            }
            case "dressing_aloe", "dressing_plantago" ->
                player.removeEffect(ModEffects.BLEEDING.get());

            default -> player.removeEffect(ModEffects.BLEEDING.get());
        }
    }

    // ── Logique Medkits ───────────────────────────────────────────────────────

    private static void handleMedkit(ServerPlayer player, String path) {
        switch (path) {
            case "medkit_novice" -> {
                player.heal(5.0f);
                player.removeEffect(ModEffects.BLEEDING.get());
                player.removeEffect(ModEffects.LACERATION.get());
            }
            case "medkit_advanced" -> {
                player.heal(10.0f);
                player.removeEffect(ModEffects.BLEEDING.get());
                player.removeEffect(ModEffects.INTERNAL_BLEEDING.get());
                player.removeEffect(ModEffects.THROMBOSIS.get());
                player.removeEffect(ModEffects.LACERATION.get());
            }
            case "medkit_expert" -> {
                player.heal(15.0f);
                for (var effect : List.of(
                    ModEffects.BLEEDING.get(), ModEffects.INTERNAL_BLEEDING.get(),
                    ModEffects.THROMBOSIS.get(), ModEffects.LACERATION.get(),
                    ModEffects.BURNS.get(), ModEffects.PARASITES.get(),
                    ModEffects.METHANOL_POISONING.get(), ModEffects.MUSHROOM_POISONING.get()
                )) player.removeEffect(effect);

                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WEAKNESS);
                player.removeEffect(MobEffects.HUNGER);
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

                if (player.hasEffect(ModEffects.BACTERIAL_INFECTION.get())) {
                    player.removeEffect(ModEffects.BACTERIAL_INFECTION.get());
                    player.addEffect(new MobEffectInstance(ModEffects.ANTIBIOTICS.get(), 1200, 0));
                }
                if (player.hasEffect(ModEffects.HPP.get())) {
                    player.removeEffect(ModEffects.HPP.get());
                    player.addEffect(new MobEffectInstance(ModEffects.HPA.get(), 1200, 0));
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void handleOpium(ServerPlayer player, int painkillerDuration, float addictionChance) {
        if (player.hasEffect(ModEffects.OPIUM_ADDICTION.get())
                || player.hasEffect(ModEffects.OPIUM_WITHDRAWAL.get())) {
            player.removeEffect(ModEffects.OPIUM_WITHDRAWAL.get());
            player.addEffect(new MobEffectInstance(ModEffects.OPIUM_ADDICTION.get(), 12000, 0));
        } else {
            if (player.getRandom().nextFloat() < addictionChance) {
                player.addEffect(new MobEffectInstance(ModEffects.OPIUM_ADDICTION.get(), 12000, 0));
            } else {
                player.addEffect(new MobEffectInstance(ModEffects.PAINKILLER.get(), painkillerDuration, 0));
            }
        }
    }

    private static void handleHerbal(ServerPlayer player) {
        float r = player.getRandom().nextFloat();
        if      (r < 0.10f) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
        }
        else if (r < 0.25f) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
        else if (r < 0.40f) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }
        else if (r < 0.55f) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
        else if (r < 0.70f) player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
        else if (r < 0.85f) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0));
        else if (r < 0.925f) player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        else                 player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
    }
}
