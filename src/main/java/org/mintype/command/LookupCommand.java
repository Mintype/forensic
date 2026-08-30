package org.mintype.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.mintype.Forensic;
import org.mintype.database.model.LogEntry;
import org.mintype.logging.LogFormatter;
import org.mintype.permission.ForensicPermissions;

import java.util.List;

public class LookupCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("lookup")
                .then(
                        Commands.argument(
                                        "range",
                                        IntegerArgumentType.integer(1)
                                )
                                .executes(context -> {

                                    ServerPlayer player =
                                            context.getSource()
                                                    .getPlayerOrException();

                                    int range =
                                            IntegerArgumentType.getInteger(
                                                    context,
                                                    "range"
                                            );

                                    return lookup(player, range);
                                })
                );
    }

    private static int lookup(
            ServerPlayer player,
            int range
    ) {

        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();

        System.out.println(x + " " + y + " " + z + " -> " + range);

        List<LogEntry> logs = Forensic.database.lookupLogs(
                player.level().dimension().identifier().getPath(),
                x,
                y,
                z,
                range,
                null, // time
                null, // player
                null, // action
                50    // limit
        );

        player.sendSystemMessage(
                LogFormatter.formatLogs(logs)
        );

        return logs.size();
    }
}