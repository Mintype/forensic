package org.mintype.inspect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InspectManager {

    private final Set<UUID> inspectors = ConcurrentHashMap.newKeySet();

    public boolean toggle(ServerPlayer player) {

        if (inspectors.contains(player.getUUID())) {
            inspectors.remove(player.getUUID());
            return false;
        }

        inspectors.add(player.getUUID());
        return true;
    }

    public boolean isInspecting(Player player) {
        return inspectors.contains(player.getUUID());
    }
}