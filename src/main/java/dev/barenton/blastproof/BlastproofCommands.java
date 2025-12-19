/**
 * Registers and handles the `/blastproof` commands for configuring the Blastproof mod.
 * <p>
 * This class defines subcommands for toggling block damage and fire creation settings,
 * providing both getter and setter functionality with tab-completion for valid keys.
 */
package dev.barenton.blastproof;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.Set;

public final class BlastproofCommands {
    private static final String LOG_PREFIX = "[Blastproof]";
    private static final PermissionCheck BLASTPROOF_PERMISSION = Commands.LEVEL_GAMEMASTERS;

    // Prevent instantiation
    private BlastproofCommands() {
    }

    /**
     * Registers all Blastproof-related commands on mod initialization.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Register commands for each configurable section
            registerConfigCommand(dispatcher, BlastproofConfig.SECTION_BLOCK_DAMAGE, "disableBlockDamage");
            registerConfigCommand(dispatcher, BlastproofConfig.SECTION_FIRE_CREATION, "disableFireCreation");
        });
    }

    /**
     * Builds and registers a subsection command under `/blastproof`.
     *
     * @param dispatcher  The brigadier command dispatcher
     * @param sectionKey  The config section identifier (e.g., block damage settings)
     * @param commandName The literal name of the subcommand (e.g., "disableBlockDamage")
     */
    private static void registerConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher,
                                              String sectionKey,
                                              String commandName) {
        dispatcher.register(
                Commands.literal("blastproof")
                        .requires(Commands.hasPermission(BLASTPROOF_PERMISSION))
                        .then(Commands.literal(commandName)
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(createSuggestionProvider(sectionKey))
                                        .executes(ctx -> handleGet(ctx, sectionKey, commandName))
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> handleSet(ctx, sectionKey, commandName))
                                        )
                                )
                        )
        );
    }

    /**
     * Handles setting a configuration entry and notifies the command source.
     *
     * @param ctx         The command context
     * @param section     The config section to modify
     * @param commandName Display name used in feedback messages
     * @return 1 on success, 0 on failure
     */
    private static int handleSet(CommandContext<CommandSourceStack> ctx,
                                 String section,
                                 String commandName) {
        String key = StringArgumentType.getString(ctx, "key");
        boolean newValue = BoolArgumentType.getBool(ctx, "value");

        // Update config; returns false if key is invalid or update failed
        if (BlastproofConfig.updateEntry(section, key, newValue)) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal(
                            String.format("%s %s for %s is now set to: %b", LOG_PREFIX, commandName, key, newValue)
                    ),
                    false
            );
            return 1;
        } else {
            ctx.getSource().sendFailure(
                    Component.literal("Failed to update configuration for: " + key)
            );
            return 0;
        }
    }

    /**
     * Handles retrieving a configuration entry and sends the current value to the command source.
     *
     * @param ctx         The command context
     * @param section     The config section to query
     * @param commandName Display name used in feedback messages
     * @return Always returns 1
     */
    private static int handleGet(CommandContext<CommandSourceStack> ctx,
                                 String section,
                                 String commandName) {
        String key = StringArgumentType.getString(ctx, "key");
        boolean value = BlastproofConfig.get(section, key, true);

        ctx.getSource().sendSuccess(
                () -> Component.literal(
                        String.format("%s %s for %s is currently set to: %b", LOG_PREFIX, commandName, key, value)
                ),
                false
        );
        return 1;
    }

    /**
     * Creates a SuggestionProvider for valid config keys in a given section.
     *
     * @param section The config section identifier
     * @return A SuggestionProvider that suggests each key from the config
     */
    private static SuggestionProvider<CommandSourceStack> createSuggestionProvider(String section) {
        return (ctx, builder) -> {
            Set<String> keys = BlastproofConfig.getKeysForSection(section);
            keys.forEach(builder::suggest);
            return builder.buildFuture();
        };
    }
}
