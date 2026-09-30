// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level.block;

import com.optica.api.mc.core.IHolderLookup;
import com.optica.api.mc.nbt.ICompoundTag;
import com.optica.api.mc.world.level.block.IBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
@SuppressWarnings("DataFlowIssue")
public abstract class BlockEntityMixin implements IBlockEntity {
    @Shadow
    public abstract CompoundTag saveWithFullMetadata(HolderLookup.Provider provider);

    @Override
    public ICompoundTag ph$saveWithFullMetadata(IHolderLookup.Provider provider) {
        return (ICompoundTag) (Object) saveWithFullMetadata((HolderLookup.Provider) provider);
    }
}
