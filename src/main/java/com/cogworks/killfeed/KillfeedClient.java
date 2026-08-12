package com.cogworks.killfeed;

import com.cogworks.killfeed.client.KillIconManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Killfeed.MODID, dist = Dist.CLIENT)
public class KillfeedClient {
    public KillfeedClient(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onRegisterReloadListeners);
    }

    private void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new KillIconManager());
    }
}