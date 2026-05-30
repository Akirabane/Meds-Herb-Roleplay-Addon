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

        // ── Dégâts par saignement / poison (intervalles individuels) ──────
        // Valeurs adoucies : ces afflictions laissent désormais le temps de réagir
        // et de se soigner avant de devenir mortelles.

        // Bleeding : 1 HP toutes les 15 s (10 s si lacération active)
        if (player.hasEffect(ModEffects.BLEEDING.get())) {
            boolean lacerated = player.hasEffect(ModEffects.LACERATION.get());
            int interval = lacerated ? 200 : 300; // 10 s / 15 s
            if (t % interval == 0) bleed(player, 1.0f);
        }

        // Internal Bleeding : 1 HP toutes les 7 s
        if (t % 140 == 0 && player.hasEffect(ModEffects.INTERNAL_BLEEDING.get())) {
            bleed(player, 1.0f);
        }

        // HPP : 1 HP toutes les 5 s — laisse ~50 s pour trouver une HPA
        if (t % 100 == 0 && player.hasEffect(ModEffects.HPP.get())) {
            bleed(player, 1.0f);
        }

        // Thrombosis : 1 HP toutes les 6 s — mortel à terme sans traitement
        if (t % 120 == 0 && player.hasEffect(ModEffects.THROMBOSIS.get())) {
            bleed(player, 1.0f);
        }

        // Methanol Poisoning : 1 HP toutes les 8 s
        if (t % 160 == 0 && player.hasEffect(ModEffects.METHANOL_POISONING.get())) {
            bleed(player, 1.0f);
        }

        // Bacterial Infection : 1 HP toutes les 10 s (progressive et lente)
        if (t % 200 == 0 && player.hasEffect(ModEffects.BACTERIAL_INFECTION.get())) {
            bleed(player, 1.0f);
        }

        // ── Broken Bone : 1 HP toutes les 4 s si en mouvement ─────────────

        if (t % 80 == 0 && player.hasEffect(ModEffects.BROKEN_BONE.get())) {
            boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.001
                          || player.getDeltaMovement().y > 0.1;
            if (moving) bleed(player, 1.0f);
        }

        // ── Burns : 1 HP toutes les 5 s ───────────────────────────────────

        if (t % 100 == 0 && player.hasEffect(ModEffects.BURNS.get())) {
            bleed(player, 1.0f);
        }

        // ── Parasites : 0.5 HP + faim toutes les 5 s ──────────────────────

        if (t % 100 == 0 && player.hasEffect(ModEffects.PARASITES.get())) {
            bleed(player, 0.5f);
            player.causeFoodExhaustion(0.5f);
        }

        // ── Mushroom Poisoning : 0.5 HP toutes les 4 s + nausée ──────────

        if (t % 80 == 0 && player.hasEffect(ModEffects.MUSHROOM_POISONING.get())) {
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

            // Broken Bone → lenteur sévère
            if (player.hasEffect(ModEffects.BROKEN_BONE.get())) {
                silent(player, MobEffects.MOVEMENT_SLOWDOWN, 100, 1); // Slowness 2
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

        // Bone Heal : supprime Broken Bone si la durée restante est presque nulle
        // (la guérison complète est gérée par l'expiration dans onEffectExpired)
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

    // Dégâts d'affliction qui ignorent les iFrames, AVEC un plancher de survie :
    // une affliction ne peut jamais tuer — elle laisse le joueur à 1 HP minimum.
    // La mort ne vient que d'une vraie source externe (chute, combat, noyade…).
    private static void bleed(ServerPlayer player, float amount) {
        if (player.getHealth() <= 1.0f) return; // déjà au plancher, on n'inflige rien
        float capped = Math.min(amount, player.getHealth() - 1.0f);
        if (capped <= 0f) return;
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().magic(), capped);
    }

    // Applique un effet vanilla silencieux (pas de particules) si absent
    private static void silent(ServerPlayer player, net.minecraft.world.effect.MobEffect effect,
                               int duration, int amp) {
        if (!player.hasEffect(effect)) {
            player.addEffect(new MobEffectInstance(effect, duration, amp, false, false));
        }
    }
}
