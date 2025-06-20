package dev.barenton.blastproof.mixin;

import dev.barenton.blastproof.BlastproofConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {
    @Redirect(
            method = "useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/phys/BlockHitResult;)"
                    + "Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;explode"
                            + "(Lnet/minecraft/world/entity/Entity;"
                            + "Lnet/minecraft/world/damagesource/DamageSource;"
                            + "Lnet/minecraft/world/level/ExplosionDamageCalculator;"
                            + "Lnet/minecraft/world/phys/Vec3;"
                            + "FZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            )
    )
    private void redirectBedExplosion(
            Level world,
            Entity source,
            DamageSource damageSource,
            ExplosionDamageCalculator calculator,
            Vec3 pos,
            float radius,
            boolean fire,
            ExplosionInteraction interaction
    ) {
        boolean disableAnchor = BlastproofConfig.get(
                BlastproofConfig.SECTION_BLOCK_DAMAGE,
                BlastproofConfig.BED_KEY,
                false
        );

        ExplosionInteraction actualInteraction = disableAnchor
                ? ExplosionInteraction.NONE
                : interaction;

        world.explode(
                source,
                damageSource,
                calculator,
                pos,
                radius,
                fire,
                actualInteraction
        );
    }
}