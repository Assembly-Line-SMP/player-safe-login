package io.SousaLJ.playersafelogin.network.payloads;

import io.SousaLJ.playersafelogin.PlayerSafeLogin;
import io.SousaLJ.playersafelogin.network.IPlayerSafeLoginPacket;
import io.SousaLJ.playersafelogin.util.AuthLog;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record PasswordAuthenticationPayload(UUID playerId, String hashedPassword) implements IPlayerSafeLoginPacket {
    /**
     * The type of the packet.
     */

    private static final Map<UUID, Long> recentFailedAttempts = new HashMap<>();
    private static final long COOLDOWN_MILLIS = 10_000; // 10 seconds

    public static final CustomPacketPayload.Type<PasswordAuthenticationPayload> TYPE = new Type<>(
            PlayerSafeLogin.rl("login_password_register")
    );


    public static final StreamCodec<FriendlyByteBuf, PasswordAuthenticationPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PasswordAuthenticationPayload::playerId,
            ByteBufCodecs.STRING_UTF8,
            PasswordAuthenticationPayload::hashedPassword,
            PasswordAuthenticationPayload::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PasswordAuthenticationPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (PlayerSafeLogin.getStorage().requiresInitialSetup(playerId)) {
                PlayerSafeLogin.getStorage().setupPassword(playerId, hashedPassword);
                PlayerSafeLogin.LOGGER.info("Password set up for player: " + playerId);
            } else {
                boolean isValid = PlayerSafeLogin.getStorage().validatePassword(playerId, hashedPassword);
                if (!isValid) {
                    ServerPlayer serverPlayer = (ServerPlayer) context.player();

                    if (serverPlayer != null) {
                        /*
                        * Start of login attempt log registration
                        * */
                        String playerName = serverPlayer.getGameProfile().getName();
                        String uuid = serverPlayer.getUUID().toString();
                        String ip = context.connection().getRemoteAddress().toString();
                        AuthLog.logFailedAttempt(playerName, uuid, ip);
                        /*
                        * End of login attempt log registration
                        * */

                        serverPlayer.connection.disconnect(Component.translatable("playersafelogin.kick.invalid_password"));
                    } else {
                        PlayerSafeLogin.LOGGER.error("Null player when trying to disconnect for invalid password.");
                    }
                } else {
                    PlayerSafeLogin.LOGGER.info("Password validated for player: " + playerId);
                }
            }
        });
    }
}
