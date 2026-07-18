package org.mintype.event;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.mintype.logging.DataBuilder;
import org.mintype.logging.LoggerService;
import org.mintype.database.model.ActionType;

public class BlockEvents {

    public static void register(LoggerService logger) {

        // Block breaking
        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    String block = state.getBlock().toString();

                    logger.log(
                            player.getUUID(),
                            ActionType.BLOCK_BREAK,
                            world.dimension().toString(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            DataBuilder.block(block)
                    );

                }
        );

//        // Block placing
//        UseBlockCallback.EVENT.register(
//                (player, world, hand, hitResult) -> {
//
//                    if (!world.isClientSide()) {
//
//                        BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
//
//                        BlockState state = world.getBlockState(pos);
//
//                        logger.log(
//                                player.getUUID(),
//                                ActionType.BLOCK_PLACE,
//                                world.dimension().toString(),
//                                pos.getX(),
//                                pos.getY(),
//                                pos.getZ(),
//                                state.getBlock().toString()
//                        );
//                    }
//
//                    return InteractionResult.PASS;
//                }
//        );
    }
}