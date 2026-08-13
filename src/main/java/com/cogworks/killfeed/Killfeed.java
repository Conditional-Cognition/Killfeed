package com.cogworks.killfeed;

import com.cogworks.killfeed.client.*;
import com.cogworks.killfeed.network.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Killfeed.MODID)
public class Killfeed {
    public static final String MODID = "killfeed";

    public Killfeed(IEventBus modEventBus, @SuppressWarnings("unused") ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onRegisterPayloads);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                KillFeedPayload.TYPE,
                KillFeedPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        KillFeedDisplay.handle(payload))
        );
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        boolean isPlayer = victim instanceof Player;
        boolean isTamedPet = victim instanceof TamableAnimal tamable && tamable.isTame();
        if (!isPlayer && !isTamedPet) return;

        String victimName = victim.getDisplayName().getString();
        String killerName = event.getSource().getEntity() != null
                ? event.getSource().getEntity().getDisplayName().getString()
                : "";
        String deathKey = event.getSource().getMsgId();

        KillFeedPayload payload = new KillFeedPayload(victimName, killerName, deathKey);

        if (isPlayer) {
            PacketDistributor.sendToPlayersInDimension((net.minecraft.server.level.ServerLevel) victim.level(), payload);
        } else {
            PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel) victim.level(), null,
                    victim.getX(), victim.getY(), victim.getZ(), 64, payload);
        }
    }
}