/**
 * Main entry point for the Blastproof Fabric mod.
 * <p>
 * Handles mod initialization by loading configuration,
 * registering commands, and logging startup events.
 */
package dev.barenton.blastproof;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Blastproof implements ModInitializer {
    /**
     * Unique identifier for the mod.
     */
    public static final String MOD_ID = "Blastproof";

    /**
     * Logger instance for mod-wide logging. Initialized by SLF4J.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Called by Fabric on mod initialization.
     * <p>
     * Loads configuration, registers commands, and logs startup.
     */
    @Override
    public void onInitialize() {
        // Load or create config file
        BlastproofConfig.load();

        // Register mod commands
        BlastproofCommands.register();

        // Log successful initialization
        getLogger().info("{} was initialized!", MOD_ID);
    }

    /**
     * Provides the shared logger instance.
     *
     * @return the SLF4J Logger for this mod
     */
    public static Logger getLogger() {
        return LOGGER;
    }
}
