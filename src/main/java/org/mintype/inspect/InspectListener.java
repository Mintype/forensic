package org.mintype.inspect;

import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.mintype.Forensic;
import org.mintype.database.model.LogEntry;
import org.mintype.logging.LogFormatter;

import java.util.List;

public class InspectListener {

    public static void register() {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            var pos = hitResult.getBlockPos();

            List<LogEntry> logs = Forensic.database.getLogs(
                    world.dimension().identifier().getPath(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    5
            );

            if (logs.isEmpty()) {
                player.sendSystemMessage(Component.literal("No logs found."));
            } else {
                player.sendSystemMessage(LogFormatter.formatLogs(logs));
            }

            return InteractionResult.SUCCESS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            List<LogEntry> logs = Forensic.database.getLogs(
                    world.dimension().identifier().getPath(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    5
            );

            if (logs.isEmpty()) {
                player.sendSystemMessage(Component.literal("No logs found."));
            } else {
                player.sendSystemMessage(LogFormatter.formatLogs(logs));
            }

            return InteractionResult.FAIL; // prevents the block from being attacked
        });

        // Prevent using items
        UseItemCallback.EVENT.register((player, world, hand) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            return InteractionResult.FAIL;
        });


        // Prevent attacking entities
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            return InteractionResult.FAIL;
        });


        // Prevent hitting entities
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!Forensic.inspectManager.isInspecting(player)) {
                return InteractionResult.PASS;
            }

            return InteractionResult.FAIL;
        });
    }
}