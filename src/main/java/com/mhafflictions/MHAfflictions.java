package com.mhafflictions;

import com.mhafflictions.registration.ModEffects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MHAfflictions.MOD_ID)
public class MHAfflictions {

    public static final String MOD_ID = "mhafflictions";
    public static final Logger LOGGER = LogManager.getLogger();

    public MHAfflictions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEffects.MOB_EFFECTS.register(modBus);
    }
}
