package io.SousaLJ.playersafelogin.client;

import io.SousaLJ.playersafelogin.PlayerSafeLogin;
import io.SousaLJ.playersafelogin.client.gui.PasswordScreen;
import io.SousaLJ.playersafelogin.network.PlayerSafeLoginPacketHandler;
import io.SousaLJ.playersafelogin.network.payloads.PasswordAuthenticationPayload;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;


@Mod(value = PlayerSafeLogin.MODID, dist = Dist.CLIENT)
public class PlayerSafeLoginClient {
    public static boolean passwordScreenCompleted = ClientPasswordManager.hasSavedPassword();

    public PlayerSafeLoginClient(IEventBus modBus) {
        PlayerSafeLogin.LOGGER.info("PlayerSafeLogin Client Initialized");
        // Register the event bus
        NeoForge.EVENT_BUS.register(this);
    }

   @SubscribeEvent
    public void onClientStarted(ScreenEvent.Opening event) {

        if(!passwordScreenCompleted && !(event.getScreen() instanceof PasswordScreen)){
            Screen prev = event.getScreen();
            event.setNewScreen(new PasswordScreen(prev));
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if(ClientPasswordManager.hasSavedPassword()){
            String storedHash = ClientPasswordManager.loadPassword();
            PasswordAuthenticationPayload payload = new PasswordAuthenticationPayload(event.getPlayer().getUUID(), storedHash);
            PlayerSafeLoginPacketHandler.sendToServer(payload);
            PlayerSafeLogin.LOGGER.info("Sending password to server");
        }
    }
}