// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.mixins.iris;

import com.optica.core.iris.IrisManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

@Mixin(ShaderProperties.class)
public abstract class ShaderPropertiesMixin {
    @Inject(
            method = "<init>(Ljava/lang/String;Lnet/irisshaders/iris/shaderpack/option/ShaderPackOptions;Ljava/lang/Iterable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Properties;forEach(Ljava/util/function/BiConsumer;)V",
                    ordinal = 0
            )
    )
    private void setupProperties(
            String contents,
            ShaderPackOptions shaderPackOptions,
            Iterable<StringPair> environmentDefines,
            CallbackInfo ci,
            @Local(name = "preprocessed") Properties preprocessed
    ) {
        IrisManager.setupProperties(preprocessed, LoggerFactory.getLogger("Iris"));
    }
}
