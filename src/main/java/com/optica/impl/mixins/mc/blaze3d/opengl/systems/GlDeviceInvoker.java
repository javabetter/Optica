package com.optica.impl.mixins.mc.blaze3d.opengl.systems;

import com.mojang.blaze3d.opengl.GlDebugLabel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * GlDevice is package-private since 26.1, so it is targeted by name. Cast a GL
 * {@code GpuDeviceBackend} to this interface to reach its debug labels.
 */
@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public interface GlDeviceInvoker {
    @Invoker("debugLabels")
    GlDebugLabel optica$debugLabels();
}
