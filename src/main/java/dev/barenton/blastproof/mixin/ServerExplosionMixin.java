/**
 * Applies Blastproof's block, fire, and mob settings to server explosions.
 */
package dev.barenton.blastproof.mixin;

import dev.barenton.blastproof.BlastproofConfig;
import dev.barenton.blastproof.BlastproofExplosionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.barenton.blastproof.BlastproofConfig.SECTION_BLOCK_DAMAGE;
import static dev.barenton.blastproof.BlastproofConfig.SECTION_FIRE_CREATION;
import static dev.barenton.blastproof.BlastproofConfig.SECTION_MOB_DAMAGE;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
    /**
     * The entity that caused this explosion (e.g., TNT entity or creeper).
     */
    @Shadow
    @Final
    private Entity source;

    /**
     * Calculator supplied for this explosion, including explicit bed/anchor type metadata.
     */
    @Shadow
    @Final
    private ExplosionDamageCalculator damageCalculator;

    /**
     * Cached explosion type key for the lifetime of this explosion instance.
     */
    @Unique
    private BlastproofExplosionType explosionTypeCache;

    /**
     * Makes configured mobs follow Minecraft's complete explosion-ignore path.
     */
    @Redirect(
            method = "hurtEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;ignoreExplosion"
                            + "(Lnet/minecraft/world/level/Explosion;)Z"
            )
    )
    private boolean ignoreExplosionForConfiguredMob(Entity entity, Explosion explosion) {
        return entity.ignoreExplosion(explosion)
                || (entity instanceof Mob
                && BlastproofConfig.get(SECTION_MOB_DAMAGE, getExplosionType().configKey(), false));
    }

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
        if (BlastproofConfig.get(section, getExplosionType().configKey(), true)) {
            ci.cancel();
        }
    }

    /**
     * Determines and caches the explosion source type for config lookup.
     *
     * @return the canonical source type for this explosion
     */
    @Unique
    private BlastproofExplosionType getExplosionType() {
        if (explosionTypeCache != null) {
            return explosionTypeCache;
        }
        return explosionTypeCache = BlastproofExplosionType.resolve(damageCalculator, source);
    }
}
