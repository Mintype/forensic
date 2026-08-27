package org.mintype.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.mintype.Forensic;
import org.mintype.database.model.ActionType;
import org.mintype.logging.DataBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Unique
    private List<ItemStack> forensic$before = new ArrayList<>();

    @Inject(
            method = "clicked",
            at = @At("HEAD")
    )
    private void forensic$captureBefore(
            int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci
    ) {

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return;
        }

        forensic$before.clear();

        for (Slot slot : menu.slots) {
            forensic$before.add(slot.getItem().copy());
        }
    }

    @Inject(
            method = "clicked",
            at = @At("RETURN")
    )
    private void forensic$captureAfter(
            int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci
    ) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        if (forensic$before.size() != menu.slots.size()) {
            return;
        }

        for (int i = 0; i < menu.slots.size(); i++) {

            Slot changedSlot = menu.slots.get(i);

            if (!(changedSlot.container instanceof ChestBlockEntity chest)) {
                continue;
            }

            BlockPos pos = chest.getBlockPos();

            ItemStack before = forensic$before.get(i);
            ItemStack after = menu.slots.get(i).getItem();

            int beforeCount = before.isEmpty() ? 0 : before.getCount();
            int afterCount = after.isEmpty() ? 0 : after.getCount();

// Same item, only the amount changed
            if (!before.isEmpty()
                    && !after.isEmpty()
                    && ItemStack.isSameItemSameComponents(before, after)) {

                int difference = afterCount - beforeCount;

                if (difference > 0) {
                    Forensic.logger.log(
                            player.getUUID(),
                            player.getName().getString(),
                            ActionType.CONTAINER_CHANGE,
                            player.level().dimension().identifier().getPath(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            DataBuilder.itemChange(
                                    "inserted",
                                    after.getItem().toString(),
                                    difference
                            )
                    );

                } else if (difference < 0) {
                    Forensic.logger.log(
                            player.getUUID(),
                            player.getName().getString(),
                            ActionType.CONTAINER_CHANGE,
                            player.level().dimension().identifier().getPath(),
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            DataBuilder.itemChange(
                                    "removed",
                                    before.getItem().toString(),
                                    -difference
                            )
                    );
                }

                continue;
            }

// Slot went from empty -> item
            if (before.isEmpty() && !after.isEmpty()) {

                Forensic.logger.log(
                        player.getUUID(),
                        player.getName().getString(),
                        ActionType.CONTAINER_CHANGE,
                        player.level().dimension().identifier().getPath(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        DataBuilder.itemChange(
                                "inserted",
                                after.getItem().toString(),
                                afterCount
                        )
                );

                continue;
            }

// Slot went from item -> empty
            if (!before.isEmpty() && after.isEmpty()) {

                Forensic.logger.log(
                        player.getUUID(),
                        player.getName().getString(),
                        ActionType.CONTAINER_CHANGE,
                        player.level().dimension().identifier().getPath(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        DataBuilder.itemChange(
                                "removed",
                                before.getItem().toString(),
                                beforeCount
                        )
                );

                continue;
            }

// Different items: log as removal + insertion
            if (!before.isEmpty() && !after.isEmpty()) {

                Forensic.logger.log(
                        player.getUUID(),
                        player.getName().getString(),
                        ActionType.CONTAINER_CHANGE,
                        player.level().dimension().identifier().getPath(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        DataBuilder.itemChange(
                                "removed",
                                before.getItem().toString(),
                                beforeCount
                        )
                );

                Forensic.logger.log(
                        player.getUUID(),
                        player.getName().getString(),
                        ActionType.CONTAINER_CHANGE,
                        player.level().dimension().identifier().getPath(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        DataBuilder.itemChange(
                                "inserted",
                                after.getItem().toString(),
                                afterCount
                        )
                );
            }

//            if (!ItemStack.matches(before, after)) {
//
//                Slot changedSlot = menu.slots.get(i);
//
//                if (!(changedSlot.container instanceof ChestBlockEntity chest)) {
//                    continue;
//                }
//
//                BlockPos pos = chest.getBlockPos();
//
//                String item;
//
//                int amount;
//                String action;
//
//                if (before.isEmpty() && !after.isEmpty()) {
//
//                    // Empty -> item
//                    action = "inserted";
//                    item = after.getItem().toString();
//                    amount = after.getCount();
//
//                } else if (!before.isEmpty() && after.isEmpty()) {
//
//                    // Item -> empty
//                    action = "removed";
//                    item = before.getItem().toString();
//                    amount = before.getCount();
//
//                } else if (!before.isEmpty() && !after.isEmpty()) {
//
//                    // Item -> same item, different amount
//                    if (ItemStack.isSameItemSameComponents(before, after)) {
//
//                        int difference = after.getCount() - before.getCount();
//
//                        if (difference > 0) {
//                            action = "added";
//                            amount = difference;
//                        } else if (difference < 0) {
//                            action = "removed";
//                            amount = -difference;
//                        } else {
//                            continue;
//                        }
//
//                        item = after.getItem().toString();
//
//                    } else {
//
//                        // Different items
//                        action = "changed";
//                        item = after.getItem().toString();
//                        amount = after.getCount();
//                    }
//
//                } else {
//                    continue;
//                }
//
//
////                System.out.println(
////                        player.getName().getString()
////                                + " "
////                                + action
////                                + " "
////                                + amount
////                                + " "
////                                + item
////                                + " at "
////                                + pos
////                );
////
////
////                Forensic.logger.log(
////                        player.getUUID(),
////                        player.getName().getString(),
////                        ActionType.CONTAINER_CHANGE,
////                        player.level().dimension().identifier().getPath(),
////                        pos.getX(),
////                        pos.getY(),
////                        pos.getZ(),
////                        DataBuilder.itemChange(
////                                action,
////                                item,
////                                amount
////                        )
////                );
//            }
        }
    }
}