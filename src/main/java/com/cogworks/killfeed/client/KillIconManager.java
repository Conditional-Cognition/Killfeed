package com.cogworks.killfeed.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillIconManager extends SimpleJsonResourceReloadListener {
    public record IconEntry(List<ResourceLocation> textures) {}

    // "deathKey|itemId" -> icon, for exact death_key + weapon_items combos
    private static final Map<String, IconEntry> COMBINED_ITEM_TO_ICON = new HashMap<>();
    // deathKey -> (tag -> icon), for death_key + weapon_tags combos
    private static final Map<String, Map<TagKey<Item>, IconEntry>> COMBINED_TAG_TO_ICON = new HashMap<>();

    // weapon-only, any death_key
    private static final Map<String, IconEntry> WEAPON_TO_ICON = new HashMap<>();
    private static final Map<TagKey<Item>, IconEntry> TAG_TO_ICON = new HashMap<>();

    // death_key-only, any weapon
    private static final Map<String, IconEntry> DEATH_KEY_TO_ICON = new HashMap<>();

    private static final IconEntry FALLBACK_ICON = new IconEntry(
            List.of(ResourceLocation.fromNamespaceAndPath("killfeed", "textures/kill_icons/unknown.png")));

    public KillIconManager() {
        super(new Gson(), "kill_icons");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        COMBINED_ITEM_TO_ICON.clear();
        COMBINED_TAG_TO_ICON.clear();
        WEAPON_TO_ICON.clear();
        TAG_TO_ICON.clear();
        DEATH_KEY_TO_ICON.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            var json = entry.getValue().getAsJsonObject();

            List<ResourceLocation> textures = new ArrayList<>();
            int i = 0;
            while (json.has("texture" + i)) {
                textures.add(ResourceLocation.parse(json.get("texture" + i).getAsString()));
                i++;
            }
            if (textures.isEmpty()) continue;

            IconEntry icon = new IconEntry(textures);

            String deathKey = json.has("death_key") ? json.get("death_key").getAsString() : null;
            boolean hasWeaponItems = json.has("weapon_items");
            boolean hasWeaponTags = json.has("weapon_tags");
            boolean hasWeapon = hasWeaponItems || hasWeaponTags;

            if (deathKey != null && hasWeapon) {
                if (hasWeaponItems) {
                    JsonObject items = json.getAsJsonObject("weapon_items");
                    for (String key : items.keySet()) {
                        COMBINED_ITEM_TO_ICON.put(deathKey + "|" + items.get(key).getAsString(), icon);
                    }
                }
                if (hasWeaponTags) {
                    JsonObject tags = json.getAsJsonObject("weapon_tags");
                    Map<TagKey<Item>, IconEntry> tagMap = COMBINED_TAG_TO_ICON.computeIfAbsent(deathKey, k -> new HashMap<>());
                    for (String key : tags.keySet()) {
                        ResourceLocation tagId = ResourceLocation.parse(tags.get(key).getAsString());
                        tagMap.put(TagKey.create(Registries.ITEM, tagId), icon);
                    }
                }
            } else if (hasWeapon) {
                if (hasWeaponItems) {
                    JsonObject items = json.getAsJsonObject("weapon_items");
                    for (String key : items.keySet()) {
                        WEAPON_TO_ICON.put(items.get(key).getAsString(), icon);
                    }
                }
                if (hasWeaponTags) {
                    JsonObject tags = json.getAsJsonObject("weapon_tags");
                    for (String key : tags.keySet()) {
                        ResourceLocation tagId = ResourceLocation.parse(tags.get(key).getAsString());
                        TAG_TO_ICON.put(TagKey.create(Registries.ITEM, tagId), icon);
                    }
                }
            } else if (deathKey != null) {
                DEATH_KEY_TO_ICON.put(deathKey, icon);
            }
        }
    }

    public static IconEntry getIcon(String deathKey, Item weaponItem) {
        String weaponId = weaponItem != null ? BuiltInRegistries.ITEM.getKey(weaponItem).toString() : null;
        var weaponHolder = weaponItem != null ? BuiltInRegistries.ITEM.wrapAsHolder(weaponItem) : null;

        // 1. exact combo: death_key + weapon_items
        if (weaponId != null) {
            IconEntry combo = COMBINED_ITEM_TO_ICON.get(deathKey + "|" + weaponId);
            if (combo != null) return combo;
        }

        // 2. combo: death_key + weapon_tags
        if (weaponHolder != null) {
            Map<TagKey<Item>, IconEntry> tagMap = COMBINED_TAG_TO_ICON.get(deathKey);
            if (tagMap != null) {
                for (var tagEntry : tagMap.entrySet()) {
                    if (weaponHolder.is(tagEntry.getKey())) return tagEntry.getValue();
                }
            }
        }

        // 3. weapon-only (any death_key)
        if (weaponId != null) {
            IconEntry exactMatch = WEAPON_TO_ICON.get(weaponId);
            if (exactMatch != null) return exactMatch;
        }
        if (weaponHolder != null) {
            for (var tagEntry : TAG_TO_ICON.entrySet()) {
                if (weaponHolder.is(tagEntry.getKey())) return tagEntry.getValue();
            }
        }

        // 4. death_key-only (any weapon)
        IconEntry exact = DEATH_KEY_TO_ICON.get(deathKey);
        if (exact != null) return exact;

        // 5. generic fallback
        IconEntry generic = DEATH_KEY_TO_ICON.get("generic");
        if (generic != null) return generic;

        // 6. hardcoded fallback
        return FALLBACK_ICON;
    }
}