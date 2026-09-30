// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.predicate;

import com.optica.api.mc.IProperty;
import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.nbt.ICompoundTag;
import com.optica.api.mc.world.level.IBlock;
import com.optica.api.mc.world.level.IBlockState;
import com.optica.api.mc.world.level.ILevelReader;
import com.optica.api.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record TagLightPredicate(
        IBlock block,
        @Nullable ICompoundTag nbt,
        Map<String, String> vagueProperties,
        int priority
) implements LightPredicate {
    public TagLightPredicate {
        Objects.requireNonNull(block, "block was null");
        Objects.requireNonNull(vagueProperties, "vagueProperties was null");
    }

    @Override
    public boolean test(@NonNls IBlockPos pos, @NonNls ILevelReader levelReader) {
        // Copied from BlockPredicateArgument.TagPredicate

        final IBlockState state = levelReader.ph$getBlockState(pos);
        if (!state.ph$is(block())) return false;

        for (Map.Entry<String, String> entry : vagueProperties.entrySet()) {
            final IProperty<?> property = block().ph$stateDefinition().ph$getProperty(entry.getKey());
            if (property == null) return false;

            final var value = property.ph$getValue(entry.getValue()).orElse(null);
            if (value == null) return false;

            if (!value.equals(state.ph$getValue(property))) return false;
        }

        if (nbt == null) return true;

        final IBlockEntity blockEntity = levelReader.ph$getBlockEntity(pos);
        if (blockEntity == null) return false;

        return ICompoundTag.isEqual(
                nbt,
                blockEntity.ph$saveWithFullMetadata(levelReader.ph$registryAccess())
        );
    }
}
