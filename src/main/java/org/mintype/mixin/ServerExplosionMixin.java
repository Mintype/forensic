package org.mintype.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import org.mintype.Forensic;
import org.mintype.database.model.ActionType;
import org.mintype.logging.DataBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.UUID;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {

    @Shadow @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Entity source;

    @Inject(
            method = "interactWithBlocks",
            at = @At("HEAD")
    )
    private void forensic$logExplosion(
            List<BlockPos> targetBlocks,
            CallbackInfo ci
    ) {

        for (BlockPos pos : targetBlocks) {

            BlockState state = level.getBlockState(pos);

            // Skip air just in case
            if (state.isAir()) {
                continue;
            }

            String cause;

            if (source == null) {
                cause = "unknown";
            } else {
                cause = BuiltInRegistries.ENTITY_TYPE
                        .getKey(source.getType())
                        .toString();
            }

            LivingEntity indirect = ((ServerExplosion)(Object) this)
                    .getIndirectSourceEntity();

            String playerName = null;
            UUID playerUUID = null;

            if (indirect instanceof Player player) {
                playerName = player.getName().getString();
                playerUUID = player.getUUID();
            }

            Forensic.logger.log(
                    playerUUID,
                    playerName,
                    ActionType.EXPLOSION,
                    level.dimension().identifier().getPath(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    DataBuilder.explosion(
                            cause,
                            BuiltInRegistries.BLOCK
                                    .getKey(state.getBlock())
                                    .getPath(),
                            indirect
                    )
            );
        }
    }
}