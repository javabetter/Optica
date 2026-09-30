// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.world.level.block.state;

import com.optica.api.mc.IProperty;
import com.optica.api.mc.world.level.block.state.IStateDefinition;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StateDefinition.class)
public abstract class StateDefinitionMixin implements IStateDefinition {
    @Shadow
    public abstract Property<?> getProperty(String string);

    @Override
    public @Nullable IProperty<?> ph$getProperty(String string) {
        return (IProperty<?>) getProperty(string);
    }
}
