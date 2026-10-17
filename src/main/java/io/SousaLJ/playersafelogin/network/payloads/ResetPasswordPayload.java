package io.SousaLJ.playersafelogin.network.payloads;

import io.SousaLJ.playersafelogin.PlayerSafeLogin;
import io.SousaLJ.playersafelogin.client.ClientPasswordManager;
import io.SousaLJ.playersafelogin.network.IPlayerSafeLoginPacket;
import io.SousaLJ.playersafelogin.network.PlayerSafeLoginPacketHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public record ResetPasswordPayload(UUID playerId, String newPassword) implements IPlayerSafeLoginPacket {
    public static final CustomPacketPayload.Type<ResetPasswordPayload> TYPE = new CustomPacketPayload.Type<>(
            PlayerSafeLogin.rl( "reset_password")
    );

    public static final StreamCodec<FriendlyByteBuf, ResetPasswordPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ResetPasswordPayload::playerId,
            ByteBufCodecs.STRING_UTF8,
            ResetPasswordPayload::newPassword,
            ResetPasswordPayload::new
    );

    @Override
    public @NotNull Type<ResetPasswordPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPasswordManager.deleteSavedPassword();
            String hashedPassword = ClientPasswordManager.hashPassword(newPassword);
            ClientPasswordManager.savePassword(hashedPassword, this.newPassword);
            PasswordAuthenticationPayload payload = new PasswordAuthenticationPayload(playerId, hashedPassword);
            PlayerSafeLoginPacketHandler.sendToServer(payload);
        });
    }
}