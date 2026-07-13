package org.mintype;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import org.mintype.database.BatchWriter;
import org.mintype.database.Database;
import org.mintype.database.LogQueue;
import org.mintype.database.SQLiteDatabase;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Forensic implements ModInitializer {
	public static final String MOD_ID = "forensic";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Database database;
    private BatchWriter writer;
    private Thread writerThread;

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing Forensic...");

        Path dbPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("forensic.db");

        database = new SQLiteDatabase(dbPath);
        database.init();

        LogQueue queue = new LogQueue();

        writer = new BatchWriter(queue, database);

        writerThread = new Thread(writer, "Forensic Writer");

        writerThread.start();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            writer.stop();
            writerThread.interrupt();
            database.close();
        });

        LOGGER.info("Initialized Forensic.");

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {

            LogEntry entry = new LogEntry(
                    0,
                    player.getUUID(),
                    ActionType.BLOCK_BREAK,
                    world.toString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    System.currentTimeMillis(),
                    state.getBlock().toString()
            );

            queue.enqueue(entry);
        });
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
