package org.mintype.logging;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerNameResolver {

    public static String getName(
            MinecraftServer server,
            UUID uuid
    ) {

        if (uuid == null) {
            return "Unknown";
        }

        ServerPlayer player = server.getPlayerList()
                .getPlayer(uuid);

        if (player != null) {
            return player.getName().getString();
        }

        return uuid.toString();
    }
}