package com.optica.core.config;

import com.optica.core.Photonics;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Optica's own settings. They are shader pack options: {@code /photonics/optica_settings.glsl} declares
 * them, so Iris lists them in the pack's settings menu on an "Optica" page (see {@link #MENU}), and they
 * are read here whenever the pack loads. Changing one reloads the pack, which applies it.
 *
 * <p>Shader packs choose the lighting mode themselves (Euphoria Patches offers BASIC and ReSTIR), so
 * Optica-only modes such as the cached mode are selected here and override the pack's choice.
 */
public final class OpticaSettings {
    public static final String LIGHTING_MODE = "OPTICA_LIGHTING_MODE";
    public static final String CACHE_REFRESH = "OPTICA_CACHE_REFRESH";
    public static final String CACHE_DETAIL = "OPTICA_CACHE_DETAIL";
    public static final String CACHE_MEMORY = "OPTICA_CACHE_MEMORY";
    public static final String CACHE_GI_SAMPLES = "OPTICA_CACHE_GI_SAMPLES";

    /** The settings menu page (an Iris screen) and its options, in order. */
    public static final String MENU = "OPTICA";
    public static final List<String> MENU_OPTIONS = List.of(LIGHTING_MODE, "<empty>", CACHE_REFRESH, CACHE_DETAIL, CACHE_MEMORY, CACHE_GI_SAMPLES);
    public static final List<String> SLIDERS = List.of(CACHE_REFRESH, CACHE_DETAIL, CACHE_MEMORY, CACHE_GI_SAMPLES);

    /** English labels for the menu (merged into every language the pack ships). */
    public static final Map<String, String> TRANSLATIONS = Map.ofEntries(
            Map.entry("screen." + MENU, "Optica"),
            Map.entry("screen." + MENU + ".comment", "Settings added by the Optica mod."),

            Map.entry("option." + LIGHTING_MODE, "Lighting Mode"),
            Map.entry("option." + LIGHTING_MODE + ".comment",
                    "Shader Pack: the mode selected in the pack's own settings (BASIC or ReSTIR). "
                            + "Cached: lighting is computed per block face and reused, refreshed in the background. "
                            + "Very cheap per frame; light and block changes appear after the refresh time, and entities cast no raytraced shadows."),
            Map.entry("value." + LIGHTING_MODE + ".0", "Shader Pack"),
            Map.entry("value." + LIGHTING_MODE + ".1", "Cached"),

            Map.entry("option." + CACHE_REFRESH, "Cache Refresh Time"),
            Map.entry("option." + CACHE_REFRESH + ".comment",
                    "Cached mode: seconds to recompute all cached lighting once. Lower reacts faster to changes, higher is cheaper."),
            Map.entry("value." + CACHE_REFRESH + ".0.25", "0.25 s"),
            Map.entry("value." + CACHE_REFRESH + ".0.5", "0.5 s"),
            Map.entry("value." + CACHE_REFRESH + ".1.0", "1 s"),
            Map.entry("value." + CACHE_REFRESH + ".2.0", "2 s"),
            Map.entry("value." + CACHE_REFRESH + ".3.0", "3 s"),
            Map.entry("value." + CACHE_REFRESH + ".5.0", "5 s"),
            Map.entry("value." + CACHE_REFRESH + ".10.0", "10 s"),

            Map.entry("option." + CACHE_DETAIL, "Cache Detail"),
            Map.entry("option." + CACHE_DETAIL + ".comment",
                    "Cached mode: lighting samples per block edge near the camera (4 = quarter-block shadows). Detail drops with distance."),

            Map.entry("option." + CACHE_MEMORY, "Cache Memory"),
            Map.entry("option." + CACHE_MEMORY + ".comment",
                    "Cached mode: GPU memory for the cache. Raise it if distant lighting flickers with high render distances."),
            Map.entry("value." + CACHE_MEMORY + ".32", "32 MB"),
            Map.entry("value." + CACHE_MEMORY + ".64", "64 MB"),
            Map.entry("value." + CACHE_MEMORY + ".128", "128 MB"),
            Map.entry("value." + CACHE_MEMORY + ".256", "256 MB"),
            Map.entry("value." + CACHE_MEMORY + ".512", "512 MB"),

            Map.entry("option." + CACHE_GI_SAMPLES, "Cache GI Rays"),
            Map.entry("option." + CACHE_GI_SAMPLES + ".comment",
                    "Cached mode: sky/GI rays per cache update. 0 turns GI off in this mode."),
            Map.entry("value." + CACHE_GI_SAMPLES + ".0", "Off")
    );

    public enum LightingMode {PACK, CACHED}

    public final LightingMode lightingMode;
    public final float cacheRefreshSeconds;
    public final int cacheDetail;
    public final int cacheMemoryMb;
    public final int cacheGiSamples;

    /** @param options looks up an option's current value (as Iris stores it), or null if unknown */
    public OpticaSettings(Function<String, String> options) {
        lightingMode = "1".equals(trim(options.apply(LIGHTING_MODE))) ? LightingMode.CACHED : LightingMode.PACK;
        cacheRefreshSeconds = clamp(parseFloat(options.apply(CACHE_REFRESH), 1.0f), 0.1f, 30.0f);
        cacheDetail = Integer.highestOneBit(clamp(parseInt(options.apply(CACHE_DETAIL), 4), 1, 8));
        cacheMemoryMb = clamp(parseInt(options.apply(CACHE_MEMORY), 64), 16, 512);
        cacheGiSamples = clamp(parseInt(options.apply(CACHE_GI_SAMPLES), 2), 0, 8);
    }

    /**
     * Applies the settings to a shader pack's (preprocessed) properties: selects an Optica-only lighting
     * mode if requested and passes the mode's settings on as {@code optica.*} keys.
     */
    public Properties applyTo(Properties packProperties) {
        Properties result = (Properties) packProperties.clone();

        String packMode = result.getProperty("photonics.lightingMode", "OFF").trim().toUpperCase(Locale.ROOT);
        boolean packUsesLighting = !packMode.equals("OFF");

        if (lightingMode == LightingMode.CACHED && packUsesLighting) {
            result.setProperty("photonics.lightingMode", "CACHED");
            result.setProperty("optica.cacheRefreshSeconds", Float.toString(cacheRefreshSeconds));
            result.setProperty("optica.cacheDetail", Integer.toString(cacheDetail));
            result.setProperty("optica.cacheCapacityLog2", Integer.toString(capacityLog2()));
            result.setProperty("optica.cacheGiSamples", Integer.toString(cacheGiSamples));
        }

        return result;
    }

    /**
     * Adds the Optica page to a pack's settings menu: a screen with Optica's options, linked from the end
     * of the pack's main screen. Packs without screens list every option on their main page already.
     */
    public static void addMenu(Properties packProperties) {
        String mainScreen = packProperties.getProperty("screen");
        if (mainScreen == null) return;

        String link = "[" + MENU + "]";
        if (!mainScreen.contains(link))
            packProperties.setProperty("screen", mainScreen.trim() + " " + link);

        packProperties.setProperty("screen." + MENU, String.join(" ", MENU_OPTIONS));
        packProperties.setProperty("screen." + MENU + ".columns", "1");

        String sliders = packProperties.getProperty("sliders", "").trim();
        packProperties.setProperty("sliders", (sliders + " " + String.join(" ", SLIDERS)).trim());
    }

    /** Cache entries are 32 bytes; the capacity is the largest power of two that fits the budget. */
    private int capacityLog2() {
        long entries = ((long) cacheMemoryMb << 20) / 32;
        return 63 - Long.numberOfLeadingZeros(entries);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return value == null ? fallback : Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            Photonics.LOGGER.warn("Invalid Optica setting value '{}'", value);
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Math.round(Float.parseFloat(value.trim()));
        } catch (NumberFormatException e) {
            Photonics.LOGGER.warn("Invalid Optica setting value '{}'", value);
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
