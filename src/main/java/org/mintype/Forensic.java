package org.mintype;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.log.Log;
import net.minecraft.resources.Identifier;

import org.mintype.database.*;
import org.mintype.database.model.ActionType;
import org.mintype.database.model.LogEntry;
import org.mintype.event.BlockEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Forensic implements ModInitializer {
	public static final String MOD_ID = "forensic";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Database database;
    private BatchWriter writer;
    private Thread writerThread;

    private LogQueue queue;
    public static LoggerService logger;

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing Forensic...");

        Path dbPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("forensic.db");

        database = new SQLiteDatabase(dbPath);
        database.init();

        queue = new LogQueue();

        logger = new LoggerService(queue);

        writer = new BatchWriter(queue, database);
        writerThread = new Thread(writer, "Forensic Writer");

        writerThread.start();

        BlockEvents.register(logger);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            writer.stop();
            writerThread.interrupt();
            database.close();
        });

        LOGGER.info("Initialized Forensic.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
