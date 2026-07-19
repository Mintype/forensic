package org.mintype.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import org.mintype.Forensic;
import org.mintype.database.model.ActionType;
import org.mintype.logging.DataBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(
            method = "place",
            at = @At("RETURN")
    )
    private void forensic$onPlace(
            BlockPlaceContext context,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!cir.getReturnValue().equals(InteractionResult.SUCCESS)) {
            return;
        }

        var player = context.getPlayer();

        if (player == null) {
            return;
        }

        var world = context.getLevel();

        if (world.isClientSide()) {
            return;
        }

        var pos = context.getClickedPos();

        Forensic.logger.log(
                player.getUUID(),
                player.getName().getString(),
                ActionType.BLOCK_PLACE,
                world.dimension().identifier().getPath(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                DataBuilder.block(
                        BuiltInRegistries.BLOCK
                                .getKey(((BlockItem)(Object) this).getBlock())
                                .getPath()
                )
        );
    }
}