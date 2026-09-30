// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level;

import com.optica.api.mc.Id;
import com.optica.api.mc.world.level.IBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(IBlock.class)
@SuppressWarnings({"DataFlowIssue", "unchecked", "rawtypes"})
public interface IBlockImpl {
    @Overwrite
    static Optional<IBlock> fromId(Id id) {
        return (Optional) BuiltInRegistries.BLOCK
                .get((Identifier) (Object) id)
                .map(Holder.Reference::value);
    }
}
