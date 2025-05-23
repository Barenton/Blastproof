/**
 * Mixin into Minecraft's ServerExplosion to conditionally cancel block damage
 * and fire creation based on the configured explosion source settings.
 */
package dev.wundasin.blastproof.mixin;

import dev.wundasin.blastproof.BlastproofConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.wundasin.blastproof.BlastproofConfig.SECTION_BLOCK_DAMAGE;
import static dev.wundasin.blastproof.BlastproofConfig.SECTION_FIRE_CREATION;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    /**
     * The entity that caused this explosion (e.g., TNT entity or creeper).
     */
    @Shadow
    @Final
    private Entity source;

    /**
     * Cached explosion type key for the lifetime of this explosion instance.
     */
    @Unique
    private String explosionTypeCache;

    /**
     * Hook at the start of block interaction. Cancels block damage if disabled in config.
     *
     * @param blocks list of blocks targeted
     * @param ci     callback info allowing cancellation
     */
    @Inject(method = "interactWithBlocks", at = @At("HEAD"), cancellable = true)
    private void onInteractWithBlocks(List<?> blocks, CallbackInfo ci) {
        cancelIfDisabled(SECTION_BLOCK_DAMAGE, ci);
    }

    /**
     * Hook at the start of fire creation. Cancels fire spawning if disabled in config.
     *
     * @param blocks list of blocks where fire would be created
     * @param ci     callback info allowing cancellation
     */
    @Inject(method = "createFire", at = @At("HEAD"), cancellable = true)
    private void onCreateFire(List<?> blocks, CallbackInfo ci) {
        cancelIfDisabled(SECTION_FIRE_CREATION, ci);
    }

    /**
     * Cancels the injection callback if the given config section is disabled
     * for the current explosion type.
     *
     * @param section the config section key (e.g. disableBlockDamage)
     * @param ci      the injection callback info to cancel
     */
    @Unique
    private void cancelIfDisabled(String section, CallbackInfo ci) {
        String type = getExplosionType();
        if (BlastproofConfig.get(section, type, true)) {
            ci.cancel();
        }
    }

    /**
     * Determines and caches the explosion source type string for config lookup.
     *
     * @return a key representing the explosion source (e.g. "tnt", "creeper", or "other")
     */
    @Unique
    private String getExplosionType() {
        if (explosionTypeCache != null) {
            return explosionTypeCache;
        }

        if (source != null) {
            var key = BuiltInRegistries.ENTITY_TYPE.getKey(source.getType());
            String path = key.getPath();
            // Map registry paths to config keys
            return explosionTypeCache = switch (path) {
                case "tnt" -> "tnt";
                case "creeper" -> "creeper";
                case "end_crystal" -> "end_crystal";
                case "small_fireball", "fireball" -> "fireball";
                case "wither" -> "wither";
                case "wither_skull" -> "wither_skull";
                default -> "other";
            };
        }

        // Fallback to 'other' if source is null or unrecognized
        return explosionTypeCache = "other";
    }
}
