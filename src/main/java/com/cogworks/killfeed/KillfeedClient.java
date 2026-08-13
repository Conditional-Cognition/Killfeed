package com.cogworks.killfeed;

import com.cogworks.killfeed.client.KillFeedOverlay;
import com.cogworks.killfeed.client.KillIconManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "killfeed", dist = Dist.CLIENT)
public class KillfeedClient {
    public KillfeedClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterReloadListeners);
        modEventBus.addListener(this::onRegisterGuiLayers);
        NeoForge.EVENT_BUS.addListener(this::onSystemChatReceived);
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

    private void onSystemChatReceived(ClientChatReceivedEvent.System event) {
        if (Config.DISPLAY_MODE.get() == Config.DisplayMode.FEED
                && KillFeedOverlay.isKnownDeathMessage(event.getMessage().getString())) {
            event.setCanceled(true);
        }
    }
}