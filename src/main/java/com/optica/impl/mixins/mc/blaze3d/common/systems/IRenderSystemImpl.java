// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc.blaze3d.common.systems;

import com.optica.api.gpu.systems.IGpuDevice;
import com.optica.api.gpu.systems.IRenderSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IRenderSystem.class)
public interface IRenderSystemImpl {
    @Overwrite
    static IGpuDevice getDevice() {
        return (IGpuDevice) RenderSystem.getDevice();
    }
}
