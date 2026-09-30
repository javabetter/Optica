package com.optica.common.mixins.iris;

import com.optica.core.config.OpticaSettings;
import net.irisshaders.iris.shaderpack.LanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Optica: adds the labels of Optica's settings page (see OpticaSettings) to the shader pack's
 * translations, in every language the pack ships (English text) and in en_us if it ships none.
 */
@Mixin(LanguageMap.class)
public abstract class LanguageMapMixin {
    @Unique
    private final Map<String, Map<String, String>> optica$merged = new HashMap<>();

    @Inject(method = "getTranslations", at = @At("RETURN"), cancellable = true)
    private void addOpticaTranslations(String language, CallbackInfoReturnable<Map<String, String>> cir) {
        Map<String, String> packTranslations = cir.getReturnValue();
        if (packTranslations == null && !"en_us".equals(language)) return;

        cir.setReturnValue(optica$merged.computeIfAbsent(language, key -> {
            Map<String, String> merged = new HashMap<>(OpticaSettings.TRANSLATIONS);
            if (packTranslations != null) merged.putAll(packTranslations);
            return Map.copyOf(merged);
        }));
    }
}
