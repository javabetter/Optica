// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc;

import com.optica.api.mc.IProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Property.class)
public abstract class PropertyMixin<T extends Comparable<T>> implements IProperty<T> {
    @Shadow
    public abstract Optional<T> getValue(String string);

    @Override
    public Optional<T> ph$getValue(String name) {
        return getValue(name);
    }
}
