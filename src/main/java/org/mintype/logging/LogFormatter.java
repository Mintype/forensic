package org.mintype.logging;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.mintype.database.model.LogEntry;

import java.util.UUID;

public class LogFormatter {

    public static Component format(LogEntry log) {

        MutableComponent message = Component.empty();

        message.append(
                Component.literal("[Forensic] ")
                        .withStyle(ChatFormatting.GRAY)
        );

        message.append(
                Component.literal(log.action().name())
                        .withStyle(getColor(log))
        );

        message.append(
                Component.literal("\nBlock: ")
                        .withStyle(ChatFormatting.WHITE)
        );

        if (log.data() != null && log.data().has("block")) {
            message.append(
                    Component.literal(
                            log.data()
                                    .get("block")
                                    .getAsString()
                    )
            );
        }

        message.append(
                Component.literal(
                        "\nPosition: "
                                + log.x()
                                + ", "
                                + log.y()
                                + ", "
                                + log.z()
                )
        );

        if (log.player() != null) {
            message.append(
                    Component.literal(
                            "\nPlayer: "
                                    + log.player()
                    )
            );
        }

        return message;
    }


    private static ChatFormatting getColor(LogEntry log) {

        return switch (log.action()) {

            case BLOCK_BREAK ->
                    ChatFormatting.RED;

            case BLOCK_PLACE ->
                    ChatFormatting.GREEN;

            case EXPLOSION ->
                    ChatFormatting.DARK_RED;

            default ->
                    ChatFormatting.WHITE;
        };
    }
}