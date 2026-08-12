package com.cogworks.killfeed;

import com.cogworks.killfeed.Killfeed;
import com.cogworks.killfeed.client.KillIconManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddClientReloadListenersEvent;

@Mod(value = Killfeed.MODID, dist = Dist.CLIENT)
public class KillfeedClient {
    public KillfeedClient(IEventBus modEventBus) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("killfeed", "kill_icons"),
                new KillIconManager()
        );
    }
}