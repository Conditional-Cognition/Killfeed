package com.cogworks.killfeed.client;

import com.cogworks.killfeed.Config;
import com.cogworks.killfeed.network.KillFeedPayload;

public class KillFeedDisplay {
    public static void handle(KillFeedPayload payload) {
        if (Config.DISPLAY_MODE.get() != Config.DisplayMode.CHAT) {
            KillFeedOverlay.addEntry(payload);
        }
    }
}