package com.yourname.zombiesrecap;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(
        modid = "zombiesrecap",
        name = "Zombies Round Recap",
        version = "1.0"
)
public class ZombiesRecapMod {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new RoundTracker());
    }
}
