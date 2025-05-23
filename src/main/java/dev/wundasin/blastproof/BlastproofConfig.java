/**
 * Manages loading, accessing, and persisting the Blastproof mod configuration as JSON.
 */
package dev.wundasin.blastproof;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlastproofConfig {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("blastproof.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Section key for toggling block damage explosion sources
     */
    public static final String SECTION_BLOCK_DAMAGE = "disableBlockDamage";
    /**
     * Section key for toggling fire creation sources
     */
    public static final String SECTION_FIRE_CREATION = "disableFireCreation";

    private static final List<String> SECTIONS = List.of(
            SECTION_BLOCK_DAMAGE,
            SECTION_FIRE_CREATION
    );

    private static final Map<String, Map<String, Boolean>> data = new HashMap<>();

    // Prevent instantiation
    private BlastproofConfig() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads configuration from disk, creating a default file if none exists.
     */
    public static void load() {
        try {
            // Ensure config directory exists
            Files.createDirectories(CONFIG_DIR);

            // Write default config if missing
            if (Files.notExists(CONFIG_PATH)) {
                saveDefault();
            }

            // Read existing config
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Map<String, Map<String, Boolean>> loaded =
                        GSON.fromJson(reader, new TypeToken<Map<String, Map<String, Boolean>>>() {
                        }.getType());

                // Initialize data with defaults then override with any loaded values
                Map<String, Map<String, Boolean>> defaults = createDefaultData();
                data.clear();
                for (String section : SECTIONS) {
                    Map<String, Boolean> sectionMap = defaults.get(section);
                    if (loaded != null && loaded.containsKey(section)) {
                        sectionMap.putAll(loaded.get(section));
                    }
                    data.put(section, sectionMap);
                }
            }

            Blastproof.getLogger().info("Blastproof config loaded!");
        } catch (IOException | JsonParseException e) {
            Blastproof.getLogger().error("Failed to load Blastproof config! Using defaults.", e);
            // Fallback to defaults in memory
            data.clear();
            data.putAll(createDefaultData());
        }
    }

    /**
     * Updates a single config entry and persists to disk.
     *
     * @param section the config section to update
     * @param key     the specific key within the section
     * @param value   the new boolean value
     * @return true if save succeeded, false otherwise
     */
    public static boolean updateEntry(String section, String key, boolean value) {
        if (!data.containsKey(section)) {
            return false;
        }
        data.get(section).put(key, value);
        return save();
    }

    /**
     * Retrieves a config value, returning defaultValue if missing.
     *
     * @param section      the config section to query
     * @param key          the specific key within the section
     * @param defaultValue fallback if key not present
     * @return the current or default boolean value
     */
    public static boolean get(String section, String key, boolean defaultValue) {
        return data.getOrDefault(section, Collections.emptyMap())
                .getOrDefault(key, defaultValue);
    }

    /**
     * @param section the section name
     * @return an unmodifiable set of keys within the section
     */
    public static Set<String> getKeysForSection(String section) {
        return Collections.unmodifiableSet(
                data.getOrDefault(section, Collections.emptyMap()).keySet()
        );
    }

    /**
     * Creates the default in-memory config structure.
     *
     * @return a map of section name → (key → default boolean value)
     */
    private static Map<String, Map<String, Boolean>> createDefaultData() {
        Map<String, Map<String, Boolean>> defaults = new LinkedHashMap<>();

        Map<String, Boolean> blockDefaults = new LinkedHashMap<>();
        blockDefaults.put("tnt", true);
        blockDefaults.put("creeper", true);
        blockDefaults.put("end_crystal", true);
        blockDefaults.put("fireball", true);
        blockDefaults.put("wither", true);
        blockDefaults.put("wither_skull", true);
        blockDefaults.put("other", true);
        defaults.put(SECTION_BLOCK_DAMAGE, blockDefaults);

        Map<String, Boolean> fireDefaults = new LinkedHashMap<>();
        fireDefaults.put("other", true);
        defaults.put(SECTION_FIRE_CREATION, fireDefaults);

        return defaults;
    }

    /**
     * Saves the current in-memory config to disk using pretty JSON.
     *
     * @return true on success, false on I/O error
     */
    private static boolean save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(data, writer);
            return true;
        } catch (IOException e) {
            Blastproof.getLogger().error("Failed to save Blastproof config!", e);
            return false;
        }
    }

    /**
     * Writes the default config file to disk without loading.
     */
    private static void saveDefault() throws IOException {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(createDefaultData(), writer);
        }
    }
}
