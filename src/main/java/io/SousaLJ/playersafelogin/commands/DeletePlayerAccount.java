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

import java.util.List;
import java.util.Objects;

public class DeletePlayerAccount implements Command<CommandSourceStack> {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("playersafelogin")
                        .then(Commands.literal("deleteaccount")
                                .requires(source -> source.hasPermission(2))
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
        Objects.requireNonNull(context.getSource().getServer().getProfileCache()).get(playerName).ifPresentOrElse(
                gameProfile -> {
                    PlayerSafeLogin.getStorage().resetPassword(gameProfile.getId());
                    context.getSource().sendSystemMessage(Component.translatable("playersafelogin.command.delete_account.success"));
                },
                () -> context.getSource().sendFailure(Component.translatable("playersafelogin.command.delete_account.player_not_found"))
        );
        return 1;
    }
}
