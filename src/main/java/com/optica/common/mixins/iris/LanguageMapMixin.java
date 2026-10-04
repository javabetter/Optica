package com.optica.common.mixins.iris;

import com.optica.core.config.OpticaSettings;
import com.optica.core.iris.IrisManager;
import net.irisshaders.iris.shaderpack.LanguageMap;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
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
    @Unique
    private Path optica$root;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void rememberRoot(Path root, CallbackInfo ci) {
        optica$root = root;
    }

    /**
     * The pack's own English labels as its Photonics patch changes them (e.g. BSL's Photonics page), or
     * an empty map. Iris reads language files straight from disk, past the patch.
     */
    @Unique
    private Map<String, String> optica$patchedEnglish() {
        var patcher = IrisManager.getShaderPatcher().orElse(null);
        if (patcher == null || !patcher.hasPatch() || optica$root == null) return Map.of();

        Path file = optica$root.resolve("en_US.lang");
        if (!Files.exists(file)) return Map.of();

        try {
            String original = Files.readString(file, StandardCharsets.UTF_8);
            String patched = patcher.patchPackFile("/lang/en_US.lang", original);
            if (patched == null || patched.equals(original)) return Map.of();

            Properties properties = new Properties();
            properties.load(new StringReader(patched));
            Map<String, String> result = new HashMap<>();
            properties.forEach((key, value) -> result.put(key.toString(), value.toString()));
            return result;
        } catch (IOException e) {
            return Map.of();
        }
    }

    @Inject(method = "getTranslations", at = @At("RETURN"), cancellable = true)
    private void addOpticaTranslations(String language, CallbackInfoReturnable<Map<String, String>> cir) {
        Map<String, String> packTranslations = cir.getReturnValue();
        if (packTranslations == null && !"en_us".equals(language)) return;

        cir.setReturnValue(optica$merged.computeIfAbsent(language, key -> {
            Map<String, String> merged = new HashMap<>(OpticaSettings.TRANSLATIONS);
            if (packTranslations != null) merged.putAll(packTranslations);
            if ("en_us".equals(language)) merged.putAll(optica$patchedEnglish());
            return Map.copyOf(merged);
        }));
    }
}
