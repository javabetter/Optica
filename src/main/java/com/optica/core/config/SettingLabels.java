package com.optica.core.config;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Optica: readable fallback text for shader pack settings without a label in the current language.
 * Iris otherwise shows the raw name, e.g. RESTIR_INITIAL_SAMPLES or photonics_sharp.
 */
public final class SettingLabels {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");

    /** Words kept in capitals (or a fixed spelling), as players know them. */
    private static final Set<String> ACRONYMS = Set.of(
            "AA", "AO", "BRDF", "CAS", "DH", "DOF", "FPS", "FXAA", "GI", "GTAO", "HDR", "HUD", "ID", "IOR",
            "LOD", "LPV", "MC", "MCBL", "PBR", "PCF", "POM", "RGB", "RT", "SMAA", "SS", "SSAO", "SSGI", "SSR",
            "SSS", "TAA", "TAAU", "UI", "UV", "VL", "VPS", "VR", "XLIGHT"
    );

    private SettingLabels() {
    }

    /** A readable version of a raw setting name, or the name unchanged if it does not look like one. */
    public static String prettify(String raw) {
        if (raw == null || !IDENTIFIER.matcher(raw).matches()) return raw;
        if (raw.equals("true")) return "On";
        if (raw.equals("false")) return "Off";

        boolean hasUnderscore = raw.indexOf('_') >= 0;
        boolean allUpper = raw.equals(raw.toUpperCase(Locale.ROOT));
        boolean allLower = raw.equals(raw.toLowerCase(Locale.ROOT));
        boolean camel = !hasUnderscore && !allUpper && !allLower && Character.isLowerCase(raw.charAt(0));
        // Already-readable text ("Off", "Medium") stays as it is.
        if (!hasUnderscore && !allUpper && !allLower && !camel) return raw;

        String spaced = camel ? CAMEL_BOUNDARY.matcher(raw).replaceAll("_") : raw;
        StringBuilder out = new StringBuilder();

        for (String word : spaced.split("_+")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');

            String upper = word.toUpperCase(Locale.ROOT);
            if (upper.equals("RESTIR")) out.append("ReSTIR");
            else if (ACRONYMS.contains(upper)) out.append(upper);
            else if (Character.isDigit(word.charAt(0))) out.append(word);
            else out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
        }

        return out.isEmpty() ? raw : out.toString();
    }
}
