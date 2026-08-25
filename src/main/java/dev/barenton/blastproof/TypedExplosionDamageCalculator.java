package dev.barenton.blastproof;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

/**
 * Carries an explicit Blastproof source type while preserving every behavior of
 * the calculator supplied by Minecraft or another mod.
 */
public final class TypedExplosionDamageCalculator extends ExplosionDamageCalculator {
    private final ExplosionDamageCalculator delegate;
    private final BlastproofExplosionType explosionType;

    private TypedExplosionDamageCalculator(
            ExplosionDamageCalculator delegate,
            BlastproofExplosionType explosionType
    ) {
        this.delegate = delegate;
        this.explosionType = explosionType;
    }

    public static TypedExplosionDamageCalculator wrap(
            ExplosionDamageCalculator calculator,
            BlastproofExplosionType explosionType
    ) {
        ExplosionDamageCalculator delegate = calculator == null
                ? new ExplosionDamageCalculator()
                : calculator;
        return new TypedExplosionDamageCalculator(delegate, explosionType);
    }

    public BlastproofExplosionType explosionType() {
        return explosionType;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(
            Explosion explosion,
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            FluidState fluidState
    ) {
        return delegate.getBlockExplosionResistance(explosion, level, pos, blockState, fluidState);
    }

    @Override
    public boolean shouldBlockExplode(
            Explosion explosion,
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            float power
    ) {
        return delegate.shouldBlockExplode(explosion, level, pos, blockState, power);
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return delegate.shouldDamageEntity(explosion, entity);
    }

    @Override
    public float getKnockbackMultiplier(Entity entity) {
        return delegate.getKnockbackMultiplier(entity);
    }

    @Override
    public float getEntityDamageAmount(Explosion explosion, Entity entity, float seenPercent) {
        return delegate.getEntityDamageAmount(explosion, entity, seenPercent);
    }
}
