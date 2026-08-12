package com.cogworks.killfeed;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Killfeed.MODID)
public class Killfeed {
    public static final String MODID = "killfeed";

    public Killfeed(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
    }
}