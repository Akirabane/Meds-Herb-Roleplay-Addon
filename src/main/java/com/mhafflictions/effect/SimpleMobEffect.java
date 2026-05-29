package com.mhafflictions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SimpleMobEffect extends MobEffect {

    public SimpleMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // Tick logic handled in EffectTickHandler
    }
}
