package com.cogworks.killfeed.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class KillIconManager extends SimpleJsonResourceReloadListener {
    private static final Map<String, ResourceLocation> DEATH_KEY_TO_ICON = new HashMap<>();
    private static final ResourceLocation FALLBACK_ICON =
            ResourceLocation.fromNamespaceAndPath("killfeed", "textures/kill_icons/unknown.png");

    public KillIconManager() {
        super(new Gson(), "kill_icons");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        DEATH_KEY_TO_ICON.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            var json = entry.getValue().getAsJsonObject();
            String deathKey = json.get("death_key").getAsString();
            ResourceLocation texture = ResourceLocation.parse(json.get("texture").getAsString());
            DEATH_KEY_TO_ICON.put(deathKey, texture);
        }
    }

    public static ResourceLocation getIcon(String deathKey) {
        return DEATH_KEY_TO_ICON.getOrDefault(deathKey, FALLBACK_ICON);
    }
}