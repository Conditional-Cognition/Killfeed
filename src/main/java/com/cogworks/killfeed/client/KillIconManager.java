package com.cogworks.killfeed.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillIconManager extends SimpleJsonResourceReloadListener {
    public record IconEntry(List<ResourceLocation> textures) {}

    private static final Map<String, IconEntry> DEATH_KEY_TO_ICON = new HashMap<>();
    private static final IconEntry FALLBACK_ICON = new IconEntry(
            List.of(ResourceLocation.fromNamespaceAndPath("killfeed", "textures/kill_icons/unknown.png")));

    public KillIconManager() {
        super(new Gson(), "kill_icons");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        DEATH_KEY_TO_ICON.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            var json = entry.getValue().getAsJsonObject();
            String deathKey = json.get("death_key").getAsString();

            List<ResourceLocation> textures = new ArrayList<>();
            int i = 0;
            while (json.has("texture" + i)) {
                textures.add(ResourceLocation.parse(json.get("texture" + i).getAsString()));
                i++;
            }
            if (textures.isEmpty()) continue;

            DEATH_KEY_TO_ICON.put(deathKey, new IconEntry(textures));
        }
    }

    public static IconEntry getIcon(String deathKey) {
        IconEntry exact = DEATH_KEY_TO_ICON.get(deathKey);
        if (exact != null) return exact;

        IconEntry generic = DEATH_KEY_TO_ICON.get("generic");
        if (generic != null) return generic;

        return FALLBACK_ICON;
    }
}