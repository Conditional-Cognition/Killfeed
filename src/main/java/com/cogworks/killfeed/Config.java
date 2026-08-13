package com.cogworks.killfeed;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public enum DisplayMode { CHAT, FEED, BOTH }

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.EnumValue<DisplayMode> DISPLAY_MODE = BUILDER
            .comment("Where kill feed entries should be shown: CHAT, FEED, or BOTH")
            .defineEnum("displayMode", DisplayMode.BOTH);

    public static final ModConfigSpec SPEC = BUILDER.build();
}