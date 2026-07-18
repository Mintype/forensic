package org.mintype.logging;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class DataBuilder {

    public static JsonObject block(String block) {
        JsonObject json = new JsonObject();

        json.addProperty("block", block);

        return json;
    }


    public static JsonObject explosion(
            String cause,
            String block,
            Entity source
    ) {
        JsonObject json = new JsonObject();

        json.addProperty("cause", cause);
        json.addProperty("block", block);

        if (source != null) {
            json.addProperty(
                    "source_type",
                    source.getType().toString()
            );

            if (source instanceof Player player) {
                json.addProperty(
                        "player",
                        player.getUUID().toString()
                );
            }
        }

        return json;
    }
}