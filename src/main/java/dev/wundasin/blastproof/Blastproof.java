package dev.wundasin.blastproof;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Blastproof implements ModInitializer {
    public static final String MOD_ID = "blastproof";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BlastproofConfig.load();
        LOGGER.info("Blastproof initialized.");
    }

    public static Logger logger() {
        if (LOGGER == null) {
            throw new IllegalStateException("Logger not yet available");
        }

        return LOGGER;
    }
}
