// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.world.level.IBlockState;
import com.optica.api.mc.world.level.ILevelReader;
import com.optica.impl.mc.world.level.SingleBlockLevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("DataFlowIssue")
@Mixin(value = ILevelReader.class)
public interface ILevelReaderImpl {
    @Overwrite
    static ILevelReader createFacade(IBlockState blockState) {
        return (ILevelReader) new SingleBlockLevelReader((BlockState) blockState);
    }
}
