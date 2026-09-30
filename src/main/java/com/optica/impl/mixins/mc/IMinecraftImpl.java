// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mixins.mc;

import com.optica.api.mc.Minecraft;
import com.optica.api.mc.world.level.ILevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Minecraft.class)
public interface IMinecraftImpl {
    @Overwrite
    static void schedule(Runnable runnable) {
        net.minecraft.client.Minecraft.getInstance()
                .execute(runnable);
    }

    @Overwrite
    static @Nullable ILevel getLevel() {
        return (ILevel) net.minecraft.client.Minecraft.getInstance()
                .level;
    }

    @Overwrite
    static Vector3d getCameraPos() {
        Vec3 position = net.minecraft.client.Minecraft.getInstance()
                .gameRenderer
                .getMainCamera()
                .position();

        return new Vector3d(position.x, position.y, position.z);
    }

    @Overwrite
    static int getRenderDistance() {
        return net.minecraft.client.Minecraft.getInstance()
                .options
                .getEffectiveRenderDistance();
    }
}
