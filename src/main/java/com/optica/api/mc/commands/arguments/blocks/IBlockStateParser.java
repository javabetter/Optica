// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.mc.commands.arguments.blocks;

import com.optica.api.mc.IProperty;
import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.core.IHolderSet;
import com.optica.api.mc.nbt.ICompoundTag;
import com.optica.api.mc.world.level.IBlock;
import com.optica.api.mc.world.level.IBlockState;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IBlockStateParser {
    interface BlockResult {
        IBlockState ph$blockState();

        Map<IProperty<?>, Comparable<?>> ph$properties();

        @Nullable ICompoundTag ph$nbt();
    }

    interface TagResult {
        IHolderSet<IBlock> ph$tag();

        Map<String, String> ph$vagueProperties();

        @Nullable ICompoundTag ph$nbt();
    }

    static Either<BlockResult, TagResult> parse(
            IHolderLookup<IBlock> holderLookup,
            StringReader stringReader,
            boolean bl
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
