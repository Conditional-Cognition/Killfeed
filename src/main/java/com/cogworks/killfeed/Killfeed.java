package com.cogworks.killfeed;

import com.cogworks.killfeed.network.KillFeedPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
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
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                KillFeedPayload.TYPE,
                KillFeedPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        com.cogworks.killfeed.client.KillFeedDisplay.handle(payload))
        );
    }

    @SuppressWarnings("IfCanBeSwitch")
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

        if (deathKey.equals("explosion") && event.getSource().getEntity() != null) {
            if (event.getSource().getEntity() instanceof net.minecraft.world.entity.monster.Creeper) {
                deathKey = "explosion_creeper";
            } else if (event.getSource().getEntity() instanceof net.minecraft.world.entity.item.PrimedTnt) {
                deathKey = "explosion_tnt";
            } else if (event.getSource().getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal) {
                deathKey = "explosion_crystal";
            } else { deathKey = "explosion_misc"; }
        }

        String weaponItemId = "";
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            var weapon = attacker.getMainHandItem();
            if (!weapon.isEmpty()) {
                weaponItemId = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
            }
        }

        KillFeedPayload payload = new KillFeedPayload(victimName, killerName, deathKey, weaponItemId);

        if (isPlayer) {
            PacketDistributor.sendToPlayersInDimension((net.minecraft.server.level.ServerLevel) victim.level(), payload);
        } else {
            PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel) victim.level(), null,
                    victim.getX(), victim.getY(), victim.getZ(), 64, payload);
        }
    }
}