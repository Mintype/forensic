package org.mintype.inspect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.mintype.Forensic;
import org.mintype.database.model.LogEntry;
import org.mintype.logging.LogFormatter;

import java.util.List;

public class InspectListener {

    public static void register() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            var pos = hitResult.getBlockPos();

            List<LogEntry> logs = Forensic.database.getLogs(
                    world.dimension().toString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    5
            );

            if (logs.isEmpty()) {

                player.sendSystemMessage(
                        Component.literal("No logs found.")
                );

            } else {

                player.sendSystemMessage(
                        Component.literal(
                                "Found " + logs.size() + " logs:"
                        )
                );

                for (LogEntry log : logs) {

                    player.sendSystemMessage(
                            LogFormatter.format(log)
                    );
                }
            }

            return InteractionResult.SUCCESS;
        });
    }
}