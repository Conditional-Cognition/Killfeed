package com.cogworks.killfeed.network;

import com.cogworks.killfeed.Killfeed;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record KillFeedPayload(String victimName, String killerName, String deathKey) implements CustomPacketPayload {
    public static final Type<KillFeedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Killfeed.MODID, "kill_feed_entry"));

    public static final StreamCodec<ByteBuf, KillFeedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, KillFeedPayload::victimName,
            ByteBufCodecs.STRING_UTF8, KillFeedPayload::killerName,
            ByteBufCodecs.STRING_UTF8, KillFeedPayload::deathKey,
            KillFeedPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}