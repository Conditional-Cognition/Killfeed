package com.cogworks.killfeed.client;

import com.cogworks.killfeed.network.KillFeedPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KillFeedOverlay {
    private record Entry(String killerName, String victimName, KillIconManager.IconEntry icon,
                         long expiresAtMillis, boolean ownKill) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Set<String> RECENT_DEATH_MESSAGES =
            Collections.newSetFromMap(new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 20;
                }
            });

    private static final long DISPLAY_DURATION_MS = 5000;
    private static final int MAX_VISIBLE_NON_OWN = 5;
    private static final int ICON_SIZE = 18;
    private static final int ICON_SPACING = 2;
    private static final int ROW_PADDING = 4;
    private static final int ROW_SPACING = 2;
    private static final int TEXT_ICON_GAP = 4;
    private static final int PANEL_BACKGROUND_COLOR = 0xC0100010;
    private static final int PANEL_BORDER_COLOR = 0x50000000;

    public static void addEntry(KillFeedPayload payload) {
        RECENT_DEATH_MESSAGES.add(payload.deathKey());

        boolean ownKill = isLocalPlayer(payload.killerName());
        var icon = KillIconManager.getIcon(payload.deathKey());
        ENTRIES.add(new Entry(payload.killerName(), payload.victimName(), icon,
                System.currentTimeMillis() + DISPLAY_DURATION_MS, ownKill));
        enforceCap();
    }

    public static boolean isKnownDeathMessage(String text) {
        return RECENT_DEATH_MESSAGES.contains(text);
    }

    private static boolean isLocalPlayer(String name) {
        var player = Minecraft.getInstance().player;
        return player != null && !name.isEmpty() && player.getGameProfile().getName().equals(name);
    }

    private static void enforceCap() {
        long nonOwnCount = ENTRIES.stream().filter(e -> !e.ownKill()).count();
        int i = 0;
        while (nonOwnCount > MAX_VISIBLE_NON_OWN && i < ENTRIES.size()) {
            if (!ENTRIES.get(i).ownKill()) {
                ENTRIES.remove(i);
                nonOwnCount--;
            } else {
                i++;
            }
        }
    }

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        var minecraft = Minecraft.getInstance();
        if (!minecraft.isPaused()) {
            long now = System.currentTimeMillis();
            ENTRIES.removeIf(entry -> entry.expiresAtMillis() <= now);
        }

        if (ENTRIES.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int panelWidth = screenWidth / 4;
        int maxPanelHeight = screenHeight / 2;
        int panelX = screenWidth - panelWidth;
        int fontHeight = font.lineHeight;
        int rowHeight = ICON_SIZE + ROW_PADDING;

        int visibleRows = Math.min(ENTRIES.size(), (maxPanelHeight - ROW_PADDING) / (rowHeight + ROW_SPACING));
        int panelHeight = ROW_PADDING + visibleRows * rowHeight + Math.max(0, visibleRows - 1) * ROW_SPACING + ROW_PADDING;

        graphics.fill(panelX, 0, panelX + panelWidth, panelHeight, PANEL_BACKGROUND_COLOR);
        graphics.fill(panelX, 0, panelX + 1, panelHeight, PANEL_BORDER_COLOR);

        int y = ROW_PADDING;
        for (Entry entry : ENTRIES) {
            if (y + rowHeight > panelHeight) break;

            int iconRowWidth = entry.icon().textures().size() * ICON_SIZE
                    + (entry.icon().textures().size() - 1) * ICON_SPACING;
            int columnWidth = (panelWidth - iconRowWidth - TEXT_ICON_GAP * 2) / 2;

            List<FormattedCharSequence> killerLines = font.split(FormattedText.of(entry.killerName()), columnWidth);
            List<FormattedCharSequence> victimLines = font.split(FormattedText.of(entry.victimName()), columnWidth);

            int iconRowX = panelX + columnWidth + TEXT_ICON_GAP;
            int iconY = y + (rowHeight - ICON_SIZE) / 2;

            int iconX = iconRowX;
            for (ResourceLocation texture : entry.icon().textures()) {
                graphics.blit(texture, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                iconX += ICON_SIZE + ICON_SPACING;
            }

            int killerTextY = y + (rowHeight - fontHeight * killerLines.size()) / 2;
            for (FormattedCharSequence line : killerLines) {
                int lineWidth = font.width(line);
                graphics.drawString(font, line, iconRowX - TEXT_ICON_GAP - lineWidth, killerTextY, 0xFFFFFF);
                killerTextY += fontHeight;
            }

            int victimTextY = y + (rowHeight - fontHeight * victimLines.size()) / 2;
            for (FormattedCharSequence line : victimLines) {
                graphics.drawString(font, line, iconRowX + iconRowWidth + TEXT_ICON_GAP, victimTextY, 0xFFFFFF);
                victimTextY += fontHeight;
            }

            y += rowHeight + ROW_SPACING;
        }
    }
}