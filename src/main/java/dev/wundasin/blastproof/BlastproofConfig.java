package dev.wundasin.blastproof;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and holding the Blastproof configuration.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Ensure config file exists (and create defaults if not).</li>
 *   <li>Load `blockDamage` and `fireCreation` settings into public maps.</li>
 *   <li>Log all actions and any parsing errors.</li>
 * </ul>
 * This class should never be instantiated.
 */
public final class BlastproofConfig {
    // --- Constants & shared objects ---;
    private static final Path CONFIG_PATH = Paths.get("config", "blastproof.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()            // output human‐readable JSON
            .create();
    // Type token for deserializing JSON sections into Map<String, Boolean>
    private static final Type BOOLEAN_MAP_TYPE = new TypeToken<Map<String, Boolean>>() {}.getType();

    /** Public view of which blocks may cause damage. */
    public static final Map<String, Boolean> blockDamage = new HashMap<>();
    /** Public view of which events may create fire. */
    public static final Map<String, Boolean> fireCreation = new HashMap<>();

    // Prevent instantiation
    private BlastproofConfig() {}

    /**
     * Load configuration from disk:
     * <ol>
     *   <li>Create parent directories & default file if missing.</li>
     *   <li>Parse JSON and populate {@link #blockDamage} and {@link #fireCreation}.</li>
     *   <li>Log each loaded value and any errors encountered.</li>
     * </ol>
     */
    public static void load() {
        try {
            ensureConfigFile();  // create default file if it doesn't exist
            Blastproof.logger().info("Loading Blastproof config...");

            // Read and parse the JSON root object
            JsonObject root = JsonParser.parseReader(
                    Files.newBufferedReader(CONFIG_PATH)
            ).getAsJsonObject();

            // Load each section into its respective map
            loadSection(root, "blockDamage", blockDamage);
            loadSection(root, "fireCreation", fireCreation);

            Blastproof.logger().info("Blastproof config loaded successfully.");
        } catch (IOException | JsonParseException e) {
            Blastproof.logger().error("Failed to load Blastproof config", e);
        }
    }

    /**
     * Ensure the config file exists on disk.
     * If missing, creates parent dirs and writes default JSON.
     *
     * @throws IOException if file operations fail
     */
    private static void ensureConfigFile() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            Blastproof.logger().info("Config file not found, creating...");
            Files.createDirectories(CONFIG_PATH.getParent());  // create config/ directory

            // Write default config JSON to disk
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(getDefaultConfig(), writer);
            }
        }
    }

    /**
     * Deserialize one section of the JSON into the given map, then log each entry.
     *
     * @param root        the parsed JSON root object
     * @param sectionName the key for this section in JSON (e.g. "blockDamage")
     * @param target      the map to populate with (String → Boolean) values
     */
    private static void loadSection(JsonObject root, String sectionName, Map<String, Boolean> target) {
        if (!root.has(sectionName)) {
            Blastproof.logger().warn("Config missing '{}' section; skipping", sectionName);
            return;
        }

        // Deserialize the entire section into a Map<String,Boolean>
        JsonObject sectionJson = root.getAsJsonObject(sectionName);
        Map<String, Boolean> sectionMap = GSON.fromJson(sectionJson, BOOLEAN_MAP_TYPE);

        // Clear old values and put all new ones, logging each
        target.clear();
        target.putAll(sectionMap);
    }

    /**
     * Build the default configuration JSON object.
     * <p>
     * Defaults:
     * <ul>
     *   <li>blockDamage: tnt=false, end_crystal=false, respawn_anchor=false, creeper=false, bed=false, fireball=false, other=false</li>
     *   <li>fireCreation: respawn_anchor=true, other=false</li>
     * </ul>
     *
     * @return a JsonObject ready to be written as the default config
     */
    private static JsonObject getDefaultConfig() {
        JsonObject root = new JsonObject();

        JsonObject defaultsBlock = new JsonObject();
        defaultsBlock.addProperty("tnt", false);
        defaultsBlock.addProperty("end_crystal", false);
        defaultsBlock.addProperty("respawn_anchor", false);
        defaultsBlock.addProperty("creeper", false);
        defaultsBlock.addProperty("bed", false);
        defaultsBlock.addProperty("fireball", false);
        defaultsBlock.addProperty("other", false);

        JsonObject defaultsFire = new JsonObject();
        defaultsFire.addProperty("respawn_anchor", true);
        defaultsFire.addProperty("other", false);

        root.add("blockDamage", defaultsBlock);
        root.add("fireCreation", defaultsFire);

        return root;
    }
}