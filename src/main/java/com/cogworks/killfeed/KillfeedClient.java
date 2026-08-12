package com.cogworks.killfeed;

import com.cogworks.killfeed.client.KillFeedOverlay;
import com.cogworks.killfeed.client.KillIconManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.resources.ResourceLocation;

@Mod(value = "killfeed", dist = Dist.CLIENT)
public class KillfeedClient {
    public KillfeedClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterReloadListeners);
        modEventBus.addListener(this::onRegisterGuiLayers);
    }

    private void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new KillIconManager());
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath("killfeed", "kill_feed"),
                (graphics, deltaTracker) -> {
                    var window = Minecraft.getInstance().getWindow();
                    KillFeedOverlay.render(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight());
                }
        );
    }
}