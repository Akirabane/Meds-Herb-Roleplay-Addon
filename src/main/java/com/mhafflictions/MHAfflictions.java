package com.mhafflictions;

import com.mhafflictions.events.InjuryEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MHAfflictions.MOD_ID)
public class MHAfflictions {

    public static final String MOD_ID = "mhafflictions";
    public static final Logger LOGGER = LogManager.getLogger();

    public MHAfflictions() {
        MinecraftForge.EVENT_BUS.register(InjuryEventHandler.class);
    }
}
