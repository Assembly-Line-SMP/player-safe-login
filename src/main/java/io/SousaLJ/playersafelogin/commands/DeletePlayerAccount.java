package io.SousaLJ.playersafelogin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.SousaLJ.playersafelogin.PlayerSafeLogin;
import io.SousaLJ.playersafelogin.util.UserCacheReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class DeletePlayerAccount implements Command<CommandSourceStack> {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("playersafelogin")
                        .then(Commands.literal("deleteaccount")
                                .requires(source -> source.hasPermission(2)
                                        && source.getEntity() instanceof ServerPlayer player
                                        && player.hasPermissions(2))
                                .then(Commands.argument("playerName", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final List<String> names = UserCacheReader.getCachedPlayerNames();
                                            for (String name : names) {
                                                if (name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                                                    builder.suggest(name);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(new DeletePlayerAccount())
                                )
                        )
        );
    }


    /**
     * Delete the player's password record in case the password is lost.
     */
    @Override
    public int run(CommandContext<CommandSourceStack> context){
        String playerName = context.getArgument("playerName", String.class);
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("playersafelogin.command.delete_account.player_not_found"));
            return 0;
        } else {
            PlayerSafeLogin.getStorage().resetPassword(player.getUUID());
            context.getSource().sendSystemMessage(Component.translatable("playersafelogin.command.delete_account.success"));
            return 1;
        }
    }
}
