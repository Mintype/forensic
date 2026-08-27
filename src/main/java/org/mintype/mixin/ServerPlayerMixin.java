package org.mintype.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mintype.Forensic;
import org.mintype.database.model.ActionType;
import org.mintype.logging.DataBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(
            method = "openMenu",
            at = @At("HEAD")
    )
    private void forensic$onOpenMenu(
            MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir
    ) {
        if (provider instanceof BlockEntity blockEntity) {
            BlockPos pos = blockEntity.getBlockPos();
            ServerLevel level = (ServerLevel) blockEntity.getLevel();
            BlockState state = level.getBlockState(pos);

            ServerPlayer player = (ServerPlayer) (Object) this;

            Forensic.logger.log(
                    player.getUUID(),
                    player.getName().getString(),
                    ActionType.CONTAINER_OPEN,
                    level.dimension().identifier().getPath(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    DataBuilder.block(
                            BuiltInRegistries.BLOCK
                                    .getKey(state.getBlock())
                                    .getPath()
                    )
            );

        }
    }
}