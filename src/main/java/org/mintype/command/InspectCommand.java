package org.mintype.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.mintype.Forensic;
import org.mintype.permission.ForensicPermissions;

public class InspectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {

        return Commands.literal("inspect")
                .requires(source ->
                        source.permissions().hasPermission(
                                ForensicPermissions.INSPECT
                        )
                )
                .executes(context -> {

                    ServerPlayer player = context.getSource().getPlayerOrException();

                    boolean enabled = Forensic.inspectManager.toggle(player);

                    MutableComponent message = Component.literal("[Forensic] ")
                            .withStyle(ChatFormatting.GOLD);

                    message.append(
                            Component.literal(
                                    enabled
                                            ? "Inspect mode enabled."
                                            : "Inspect mode disabled."
                            ).withStyle(ChatFormatting.WHITE)
                    );

                    player.sendSystemMessage(message);

                    return 1;
                });
    }
}