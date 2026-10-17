package io.SousaLJ.playersafelogin.network;

import io.SousaLJ.playersafelogin.network.payloads.PasswordAuthenticationPayload;
import io.SousaLJ.playersafelogin.network.payloads.ResetPasswordPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerSafeLoginPacketHandler extends BasePacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    public PlayerSafeLoginPacketHandler(IEventBus modEventBus) {
        super(modEventBus, PROTOCOL_VERSION);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play( PasswordAuthenticationPayload.TYPE, PasswordAuthenticationPayload.STREAM_CODEC );
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
        registrar.play(ResetPasswordPayload.TYPE, ResetPasswordPayload.STREAM_CODEC);
    }

    // Métodos auxiliares para envio
    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToClient(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}