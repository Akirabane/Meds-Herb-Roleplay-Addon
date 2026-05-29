package com.mhafflictions.registration;

import com.mhafflictions.MHAfflictions;
import com.mhafflictions.effect.SimpleMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MHAfflictions.MOD_ID);

    // ── Harmful ──────────────────────────────────────────────────────────────
    public static final RegistryObject<MobEffect> BLEEDING =
        harm("bleeding", 0xCC1111);
    public static final RegistryObject<MobEffect> INTERNAL_BLEEDING =
        harm("internal_bleeding", 0x880000);
    public static final RegistryObject<MobEffect> BROKEN_BONE =
        harm("broken_bone", 0x888888);
    public static final RegistryObject<MobEffect> BLOOD_LOSS =
        harm("blood_loss", 0x660033);
    public static final RegistryObject<MobEffect> THROMBOSIS =
        harm("thrombosis", 0x220066);
    public static final RegistryObject<MobEffect> BACTERIAL_INFECTION =
        harm("bacterial_infection", 0x88AA00);
    public static final RegistryObject<MobEffect> LACERATION =
        harm("laceration", 0xFF4400);
    public static final RegistryObject<MobEffect> BURNS =
        harm("burns", 0xFF8800);
    public static final RegistryObject<MobEffect> HPP =
        harm("hpp", 0x00CC00);
    public static final RegistryObject<MobEffect> PARASITES =
        harm("parasites", 0x884400);
    public static final RegistryObject<MobEffect> METHANOL_POISONING =
        harm("methanol_poisoning", 0x00AA88);
    public static final RegistryObject<MobEffect> MUSHROOM_POISONING =
        harm("mushroom_poisoning", 0x884488);
    public static final RegistryObject<MobEffect> OPIUM_ADDICTION =
        harm("opium_addiction", 0x440066);
    public static final RegistryObject<MobEffect> OPIUM_WITHDRAWAL =
        harm("opium_withdrawal", 0x666688);
    public static final RegistryObject<MobEffect> EFFECT_BELLADONNA_BERRY =
        harm("effect_belladonna_berry", 0xFF88AA);
    public static final RegistryObject<MobEffect> ULTRAVIOLET_VULNERABILITY =
        harm("ultraviolet_vulnerability", 0x8844FF);

    // ── Beneficial ───────────────────────────────────────────────────────────
    public static final RegistryObject<MobEffect> HPA =
        benefit("hpa", 0x00FFFF);
    public static final RegistryObject<MobEffect> PAINKILLER =
        benefit("painkiller", 0xAA44FF);
    public static final RegistryObject<MobEffect> ADRENALINE =
        benefit("adrenaline", 0xFFFF00);
    public static final RegistryObject<MobEffect> ANTIBIOTICS =
        benefit("antibiotics", 0x44AAAA);
    public static final RegistryObject<MobEffect> ANTISEPTIC =
        benefit("antiseptic", 0x4488FF);
    public static final RegistryObject<MobEffect> IMMUNE =
        benefit("immune", 0xFFCC00);
    public static final RegistryObject<MobEffect> BONE_HEAL =
        benefit("bone_heal", 0x88FF88);
    public static final RegistryObject<MobEffect> BEVERAGE_DRINK =
        benefit("beverage_drink", 0xCCAA66);

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static RegistryObject<MobEffect> harm(String name, int color) {
        return MOB_EFFECTS.register(name, () -> new SimpleMobEffect(MobEffectCategory.HARMFUL, color));
    }

    private static RegistryObject<MobEffect> benefit(String name, int color) {
        return MOB_EFFECTS.register(name, () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, color));
    }
}
