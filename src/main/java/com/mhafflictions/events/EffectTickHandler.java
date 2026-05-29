package com.mhafflictions.events;

import com.mhafflictions.MHAfflictions;
import com.mhafflictions.registration.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MHAfflictions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long t = player.level().getGameTime();

        // ── Dégâts chaque seconde (20 ticks) ──────────────────────────────

        if (t % 20 == 0) {

            // Bleeding : 1 HP/s (1.5 si lacération active)
            if (player.hasEffect(ModEffects.BLEEDING.get())) {
                float dmg = player.hasEffect(ModEffects.LACERATION.get()) ? 1.5f : 1.0f;
                bleed(player, dmg);
            }

            // Internal Bleeding : 2 HP/s
            if (player.hasEffect(ModEffects.INTERNAL_BLEEDING.get())) {
                bleed(player, 2.0f);
            }

            // HPP : 2 HP/s — mort en ~10 s sans HPA
            if (player.hasEffect(ModEffects.HPP.get())) {
                bleed(player, 2.0f);
            }

            // Thrombosis : 1.5 HP/s — mortel sans traitement
            if (player.hasEffect(ModEffects.THROMBOSIS.get())) {
                bleed(player, 1.5f);
            }

            // Methanol Poisoning : 1 HP/s
            if (player.hasEffect(ModEffects.METHANOL_POISONING.get())) {
                bleed(player, 1.0f);
            }

            // Bacterial Infection : 0.5 HP/s progressive
            if (player.hasEffect(ModEffects.BACTERIAL_INFECTION.get())) {
                bleed(player, 0.5f);
            }
        }

        // ── Broken Bone : 1 HP/s si en mouvement ──────────────────────────

        if (t % 20 == 0 && player.hasEffect(ModEffects.BROKEN_BONE.get())) {
            boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.001
                          || player.getDeltaMovement().y > 0.1;
            if (moving && player.getHealth() > 2.0f) {
                bleed(player, 1.0f);
            }
        }

        // ── Burns : 1 HP toutes les 2 s ───────────────────────────────────

        if (t % 40 == 0 && player.hasEffect(ModEffects.BURNS.get())) {
            bleed(player, 1.0f);
        }

        // ── Parasites : 0.5 HP + faim toutes les 3 s ──────────────────────

        if (t % 60 == 0 && player.hasEffect(ModEffects.PARASITES.get())) {
            bleed(player, 0.5f);
            player.causeFoodExhaustion(0.5f);
        }

        // ── Mushroom Poisoning : 0.5 HP toutes les 2 s + nausée ──────────

        if (t % 40 == 0 && player.hasEffect(ModEffects.MUSHROOM_POISONING.get())) {
            bleed(player, 0.5f);
            if (!player.hasEffect(MobEffects.CONFUSION)) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
            }
        }

        // ── Effets passifs (réappliqués silencieusement) ──────────────────

        if (t % 10 == 0) {

            // Blood Loss → faiblesse + lenteur
            if (player.hasEffect(ModEffects.BLOOD_LOSS.get())) {
                silent(player, MobEffects.WEAKNESS, 100, 0);
                silent(player, MobEffects.MOVEMENT_SLOWDOWN, 100, 0);
                silent(player, MobEffects.CONFUSION, 100, 0);
            }

            // Adrenaline → vitesse + résistance
            if (player.hasEffect(ModEffects.ADRENALINE.get())) {
                silent(player, MobEffects.MOVEMENT_SPEED, 100, 1);
                silent(player, MobEffects.DAMAGE_RESISTANCE, 100, 0);
            }

            // Opium Withdrawal → faiblesse + lenteur intense
            if (player.hasEffect(ModEffects.OPIUM_WITHDRAWAL.get())) {
                silent(player, MobEffects.WEAKNESS, 100, 1);
                silent(player, MobEffects.MOVEMENT_SLOWDOWN, 100, 1);
            }

            // Belladonna Berry → désorientation
            if (player.hasEffect(ModEffects.EFFECT_BELLADONNA_BERRY.get())) {
                silent(player, MobEffects.CONFUSION, 100, 0);
                silent(player, MobEffects.BLINDNESS, 100, 0);
            }
        }

        // ── Bone Heal + Broken Bone : guérison 2× plus rapide ────────────

        if (t % 20 == 0
                && player.hasEffect(ModEffects.BONE_HEAL.get())
                && player.hasEffect(ModEffects.BROKEN_BONE.get())) {
            MobEffectInstance bone = player.getEffect(ModEffects.BROKEN_BONE.get());
            if (bone != null) {
                // Réduire la durée de 1 tick supplémentaire (total -2/tick au lieu de -1/tick)
                int reduced = Math.max(0, bone.getDuration() - 20);
                if (reduced == 0) {
                    player.removeEffect(ModEffects.BROKEN_BONE.get());
                } else {
                    player.addEffect(new MobEffectInstance(
                        ModEffects.BROKEN_BONE.get(), reduced, bone.getAmplifier(), false, true));
                }
            }
        }
    }

    // ── Expiration des effets ──────────────────────────────────────────────────

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null) return;

        // Addiction à l'opium → sevrage
        if (inst.getEffect() == ModEffects.OPIUM_ADDICTION.get()) {
            player.addEffect(new MobEffectInstance(ModEffects.OPIUM_WITHDRAWAL.get(), 12000, 0));
        }

        // Bone Heal expire → retire l'os cassé s'il reste
        if (inst.getEffect() == ModEffects.BONE_HEAL.get()) {
            player.removeEffect(ModEffects.BROKEN_BONE.get());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Dégâts qui ignorent les iFrames (saignement interne, etc.)
    private static void bleed(ServerPlayer player, float amount) {
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().magic(), amount);
    }

    // Applique un effet vanilla silencieux (pas de particules) si absent
    private static void silent(ServerPlayer player, net.minecraft.world.effect.MobEffect effect,
                               int duration, int amp) {
        if (!player.hasEffect(effect)) {
            player.addEffect(new MobEffectInstance(effect, duration, amp, false, false));
        }
    }
}
