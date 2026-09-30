// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.predicate;
import com.optica.api.mc.commands.arguments.blocks.IBlockStateParser;
import com.optica.api.mc.core.IBlockPos;
import com.optica.api.mc.world.level.IBlock;
import com.optica.api.mc.world.level.ILevelReader;
import org.jetbrains.annotations.NonNls;

import java.util.Objects;

public record BasicLightPredicate(
        @NonNls IBlock block,
        int priority
) implements LightPredicate {
    public BasicLightPredicate {
        Objects.requireNonNull(block, "block was null");
    }

    @Override
    public boolean test(@NonNls IBlockPos pos, @NonNls ILevelReader levelReader) {
        return levelReader.ph$getBlockState(pos).ph$is(this.block);
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.BlockResult blockResult) {
        return blockResult.ph$properties().isEmpty() && (blockResult.ph$nbt() == null || blockResult.ph$nbt().ph$isEmpty());
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.TagResult tagResult) {
        return tagResult.ph$vagueProperties().isEmpty() && (tagResult.ph$nbt() == null || tagResult.ph$nbt().ph$isEmpty());
    }
}
