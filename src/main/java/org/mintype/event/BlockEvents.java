package org.mintype.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import org.mintype.database.LoggerService;
import org.mintype.database.model.ActionType;

public class BlockEvents {

    public static void register(LoggerService logger) {

        // Block breaking
        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    logger.log(
                            player.getUUID(),
                            ActionType.BLOCK_BREAK,
                            world.toString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            state.getBlock().toString()
                    );

                }
        );

        // Block placing
        UseBlockCallback.EVENT.register(
                (player, world, hand, hitResult) -> {

                    if (!world.isClientSide()) {

                        BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());

                        BlockState state = world.getBlockState(pos);

                        logger.log(
                                player.getUUID(),
                                ActionType.BLOCK_PLACE,
                                world.toString(),
                                pos.getX(),
                                pos.getY(),
                                pos.getZ(),
                                state.getBlock().toString()
                        );
                    }

                    return InteractionResult.PASS;
                }
        );
    }
}