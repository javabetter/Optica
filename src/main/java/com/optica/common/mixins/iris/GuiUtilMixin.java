package com.optica.common.mixins.iris;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.optica.core.config.SettingLabels;
import net.irisshaders.iris.gui.GuiUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Optica: shader pack settings that have no label in the current language (common with packs that only
 * ship English labels, or patches like Photonics' BSL one) show a readable version of their name
 * instead of e.g. RESTIR_INITIAL_SAMPLES.
 */
@Mixin(value = GuiUtil.class, remap = false)
public abstract class GuiUtilMixin {
    @ModifyReturnValue(method = "translateOrDefault", at = @At("RETURN"))
    private static MutableComponent optica$readableFallback(MutableComponent result, MutableComponent defaultText, String translationDesc, Object... format) {
        if (result != defaultText || translationDesc == null || I18n.exists(translationDesc)) return result;
        if (!(translationDesc.startsWith("option.") || translationDesc.startsWith("screen.") || translationDesc.startsWith("value."))) return result;

        String raw = defaultText.getString();
        String readable = SettingLabels.prettify(raw);
        return readable.equals(raw) ? result : Component.literal(readable).withStyle(defaultText.getStyle());
    }
}
