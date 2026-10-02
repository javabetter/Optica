package com.optica.core.config;

import com.optica.core.Photonics;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Optica's own settings. They are shader pack options: {@code /photonics/optica_settings.glsl} declares
 * them, so Iris lists them in the pack's settings menu, and they are read here whenever the pack loads.
 * Changing one reloads the pack, which applies it.
 *
 * <p>The options go on the pack's own Photonics page when it has one (Euphoria Patches: Configure
 * Euphoria Patches > Modded Settings > Photonics), otherwise on an "Optica" page linked from the end of
 * the main page. Shader packs choose the lighting mode themselves (Euphoria Patches offers BASIC and
 * ReSTIR), so the lighting cache, an Optica-only mode, is switched on here and overrides the pack's choice.
 */
public final class OpticaSettings {
    public static final String LIGHTING_CACHE = "OPTICA_LIGHTING_CACHE";
    public static final String CACHE_REFRESH = "OPTICA_CACHE_REFRESH";
    public static final String CACHE_DETAIL = "OPTICA_CACHE_DETAIL";
    public static final String CACHE_MEMORY = "OPTICA_CACHE_MEMORY";
    public static final String CACHE_GI_SAMPLES = "OPTICA_CACHE_GI_SAMPLES";
    public static final String CACHE_PROFILER = "OPTICA_CACHE_PROFILER";
    public static final String CACHE_DEBUG_VIEW = "OPTICA_CACHE_DEBUG_VIEW";
    public static final String LOD_QUALITY = "OPTICA_LOD_QUALITY";
    public static final String LIGHT_MERGING = "OPTICA_LIGHT_MERGING";
    public static final String SHADOW_UPDATE = "OPTICA_SHADOW_UPDATE";

    /** Menu pages (Iris screens). */
    public static final String MENU = "OPTICA";
    public static final String CACHE_MENU = "OPTICA_CACHE";

    /** Optica's entries on the pack's Photonics page (two columns), or on its own page. */
    private static final List<String> PAGE_ENTRIES = List.of(
            "<empty>", "<empty>",
            LIGHTING_CACHE, "[" + CACHE_MENU + "]",
            LOD_QUALITY, LIGHT_MERGING,
            SHADOW_UPDATE
    );
    private static final List<String> CACHE_PAGE_ENTRIES = List.of(
            "<empty>", "<empty>",
            LIGHTING_CACHE, "<empty>",
            CACHE_REFRESH, CACHE_DETAIL,
            CACHE_MEMORY, CACHE_GI_SAMPLES,
            "<empty>", "<empty>",
            CACHE_PROFILER, CACHE_DEBUG_VIEW
    );
    public static final List<String> SLIDERS = List.of(
            LOD_QUALITY, LIGHT_MERGING, SHADOW_UPDATE, CACHE_REFRESH, CACHE_DETAIL, CACHE_MEMORY, CACHE_GI_SAMPLES
    );

    /** English labels for the menu (merged into every language the pack ships). */
    public static final Map<String, String> TRANSLATIONS = translations();

    public final boolean lightingCache;
    public final float cacheRefreshSeconds;
    public final int cacheDetail;
    public final int cacheMemoryMb;
    public final int cacheGiSamples;
    public final boolean cacheProfiler;
    public final int cacheDebugView;
    public final float lodQuality;
    public final int lightMerging;
    public final int shadowUpdateInterval;

    /** @param options looks up an option's current value (as Iris stores it), or null if unknown */
    public OpticaSettings(Function<String, String> options) {
        lightingCache = "1".equals(trim(options.apply(LIGHTING_CACHE)));
        cacheRefreshSeconds = clamp(parseFloat(options.apply(CACHE_REFRESH), 1.0f), 0.1f, 30.0f);
        cacheDetail = Integer.highestOneBit(clamp(parseInt(options.apply(CACHE_DETAIL), 4), 1, 8));
        cacheMemoryMb = clamp(parseInt(options.apply(CACHE_MEMORY), 64), 16, 512);
        cacheGiSamples = clamp(parseInt(options.apply(CACHE_GI_SAMPLES), 0), 0, 8);
        cacheProfiler = "1".equals(trim(options.apply(CACHE_PROFILER)));
        cacheDebugView = clamp(parseInt(options.apply(CACHE_DEBUG_VIEW), 0), 0, 2);
        lodQuality = clamp(parseFloat(options.apply(LOD_QUALITY), 1.0f), 0.1f, 1.0f);
        lightMerging = clamp(parseInt(options.apply(LIGHT_MERGING), 0), 0, 4);
        shadowUpdateInterval = clamp(parseInt(options.apply(SHADOW_UPDATE), 1), 1, 16);
    }

    /**
     * Applies the settings to a shader pack's (preprocessed) properties: passes the performance settings
     * on as {@code optica.*} keys and selects the cached lighting mode if the lighting cache is on.
     */
    public Properties applyTo(Properties packProperties) {
        Properties result = (Properties) packProperties.clone();

        result.setProperty("optica.lodScale", Float.toString(lodScale()));
        result.setProperty("optica.lightMerging", Integer.toString(lightMerging));
        result.setProperty("optica.shadowUpdateInterval", Integer.toString(shadowUpdateInterval));

        String packMode = result.getProperty("photonics.lightingMode", "OFF").trim().toUpperCase(Locale.ROOT);
        boolean packUsesLighting = !packMode.equals("OFF");

        if (lightingCache && packUsesLighting) {
            result.setProperty("photonics.lightingMode", "CACHED");
            result.setProperty("optica.cacheRefreshSeconds", Float.toString(cacheRefreshSeconds));
            result.setProperty("optica.cacheDetail", Integer.toString(cacheDetail));
            result.setProperty("optica.cacheCapacityLog2", Integer.toString(capacityLog2()));
            result.setProperty("optica.cacheGiSamples", Integer.toString(cacheGiSamples));
            result.setProperty("optica.cacheProfiler", Boolean.toString(cacheProfiler));
            result.setProperty("optica.cacheDebugView", Integer.toString(cacheDebugView));
        }

        return result;
    }

    /**
     * Adds Optica's options to a pack's settings menu: on the pack's Photonics page if it has one,
     * otherwise on an Optica page linked from the end of the main page. Packs without screens list every
     * option on their main page already.
     */
    public static void addMenu(Properties packProperties) {
        String mainScreen = packProperties.getProperty("screen");
        if (mainScreen == null) return;

        String photonicsScreen = findPhotonicsScreen(packProperties);

        if (photonicsScreen != null) {
            String entries = packProperties.getProperty(photonicsScreen).trim();
            if (!entries.contains(LIGHTING_CACHE)) {
                // Keep Optica's entries in whole rows of a two-column page.
                int count = entries.isEmpty() ? 0 : entries.split("\\s+").length;
                String padding = count % 2 == 1 ? " <empty>" : "";
                packProperties.setProperty(photonicsScreen, entries + padding + " " + String.join(" ", PAGE_ENTRIES));
            }

            // Iris lays out long pages in three columns unless told otherwise; the pack's layout (and
            // Optica's rows) are made for two.
            if (!packProperties.containsKey(photonicsScreen + ".columns"))
                packProperties.setProperty(photonicsScreen + ".columns", "2");
        } else {
            String link = "[" + MENU + "]";
            if (!mainScreen.contains(link))
                packProperties.setProperty("screen", mainScreen.trim() + " " + link);

            packProperties.setProperty("screen." + MENU, String.join(" ", PAGE_ENTRIES.subList(2, PAGE_ENTRIES.size())));
            packProperties.setProperty("screen." + MENU + ".columns", "2");
        }

        packProperties.setProperty("screen." + CACHE_MENU, String.join(" ", CACHE_PAGE_ENTRIES));
        packProperties.setProperty("screen." + CACHE_MENU + ".columns", "2");

        String sliders = packProperties.getProperty("sliders", "").trim();
        packProperties.setProperty("sliders", (sliders + " " + String.join(" ", SLIDERS)).trim());
    }

    /**
     * The pack's Photonics settings page: a screen whose name mentions Photonics (the shortest such name,
     * so sub-pages like PHOTONICS_SETTINGS_BASIC lose to PHOTONICS_SETTINGS), or null.
     */
    private static String findPhotonicsScreen(Properties packProperties) {
        String best = null;

        for (String key : packProperties.stringPropertyNames()) {
            if (!key.startsWith("screen.")) continue;

            String name = key.substring("screen.".length());
            if (name.contains(".") || !name.toUpperCase(Locale.ROOT).contains("PHOTONICS")) continue;

            if (best == null || key.length() < best.length() || (key.length() == best.length() && key.compareTo(best) < 0))
                best = key;
        }

        return best;
    }

    /**
     * The LOD Quality setting as a multiplier of the level of detail distances: 0.5 gives the distances
     * Optica's LOD was tuned with (1x), each 0.1 step multiplies them by about 1.3, and 1.0 (the default)
     * turns the level of detail off.
     */
    private float lodScale() {
        if (lodQuality >= 0.999f) return 1000.0f;
        return (float) Math.pow(16.0, lodQuality - 0.5);
    }

    /** Cache entries are 32 bytes; the capacity is the largest power of two that fits the budget. */
    private int capacityLog2() {
        long entries = ((long) cacheMemoryMb << 20) / 32;
        return 63 - Long.numberOfLeadingZeros(entries);
    }

    private static Map<String, String> translations() {
        Map<String, String> t = new HashMap<>();

        t.put("screen." + MENU, "Optica");
        t.put("screen." + MENU + ".comment", "Settings added by the Optica mod.");
        t.put("screen." + CACHE_MENU, "Lighting Cache Settings");
        t.put("screen." + CACHE_MENU + ".comment", "Settings of the lighting cache (Optica).");

        t.put("option." + LIGHTING_CACHE, "Lighting Cache");
        t.put("option." + LIGHTING_CACHE + ".comment",
                "Optica: computes lighting per block face and reuses it, refreshed in the background, instead of every frame. "
                        + "Much cheaper; light and block changes appear after the refresh time, and entities cast no raytraced shadows. "
                        + "Replaces the lighting mode above while on.");
        t.put("value." + LIGHTING_CACHE + ".0", "Off");
        t.put("value." + LIGHTING_CACHE + ".1", "On");

        t.put("option." + CACHE_REFRESH, "Refresh Time");
        t.put("option." + CACHE_REFRESH + ".comment",
                "Seconds to recompute all cached lighting once. Lower reacts faster to changes, higher is cheaper.");
        for (String v : List.of("0.25", "0.5", "1.0", "2.0", "3.0", "5.0", "10.0"))
            t.put("value." + CACHE_REFRESH + "." + v, (v.endsWith(".0") ? v.substring(0, v.length() - 2) : v) + " s");

        t.put("option." + CACHE_DETAIL, "Cache Detail");
        t.put("option." + CACHE_DETAIL + ".comment",
                "Most lighting samples per block edge, used up close (4 = quarter-block shadows). Further away, detail follows how big blocks are on screen.");

        t.put("option." + CACHE_MEMORY, "Cache Memory");
        t.put("option." + CACHE_MEMORY + ".comment",
                "GPU memory for the cache. Raise it if distant lighting flickers with high render distances.");
        for (String v : List.of("32", "64", "128", "256", "512"))
            t.put("value." + CACHE_MEMORY + "." + v, v + " MB");

        t.put("option." + CACHE_GI_SAMPLES, "Cache GI");
        t.put("option." + CACHE_GI_SAMPLES + ".comment",
                "Screen Space: global illumination (bounced light) as in Sharp mode, smooth and computed every frame. "
                        + "1 / 2 / 4 Rays: GI stored in the cache with that many rays per update; cheaper, but it can look blotchy.");
        t.put("value." + CACHE_GI_SAMPLES + ".0", "Screen Space");
        t.put("value." + CACHE_GI_SAMPLES + ".1", "1 Ray");
        t.put("value." + CACHE_GI_SAMPLES + ".2", "2 Rays");
        t.put("value." + CACHE_GI_SAMPLES + ".4", "4 Rays");

        t.put("option." + CACHE_PROFILER, "Profiler");
        t.put("option." + CACHE_PROFILER + ".comment",
                "Diagnostics: records what the lighting cache does each second to optica-profile.log in the Minecraft folder. "
                        + "Costs some performance; leave it off unless you are collecting a report.");
        t.put("value." + CACHE_PROFILER + ".0", "Off");
        t.put("value." + CACHE_PROFILER + ".1", "On");

        t.put("option." + CACHE_DEBUG_VIEW, "Debug View");
        t.put("option." + CACHE_DEBUG_VIEW + ".comment",
                "Diagnostics: colours the lighting. Cache Status: green = cached, yellow = partly cached, red = keeping the previous "
                        + "lighting, magenta = stand-in lighting, blue = hand. Detail Level: red/yellow/green/cyan = 1/2/4/8 samples "
                        + "per block edge, blue = one sample per block.");
        t.put("value." + CACHE_DEBUG_VIEW + ".0", "Off");
        t.put("value." + CACHE_DEBUG_VIEW + ".1", "Cache Status");
        t.put("value." + CACHE_DEBUG_VIEW + ".2", "Detail Level");

        t.put("option." + LOD_QUALITY, "LOD Quality");
        t.put("option." + LOD_QUALITY + ".comment",
                "Optica: how far lighting keeps full detail before it gets cheaper (fewer lights, less frequent updates, coarser GI and cache). "
                        + "Lower is faster. 1.0 is maximum quality (no level of detail).");
        for (int i = 1; i <= 10; i++) {
            String v = i == 10 ? "1.0" : "0." + i;
            t.put("value." + LOD_QUALITY + "." + v, i == 10 ? "1.0 (Max)" : v);
        }

        t.put("option." + LIGHT_MERGING, "Light Merging");
        t.put("option." + LIGHT_MERGING + ".comment",
                "Optica: merges dense groups of the same light (lava lakes, glowstone ceilings) into fewer lights, more so further away. "
                        + "Higher is faster but less precise; Off keeps every light.");
        List<String> mergingNames = List.of("Off", "Low", "Medium", "High", "Maximum");
        for (int i = 0; i < mergingNames.size(); i++)
            t.put("value." + LIGHT_MERGING + "." + i, mergingNames.get(i));

        t.put("option." + SHADOW_UPDATE, "Shadow Update Interval");
        t.put("option." + SHADOW_UPDATE + ".comment",
                "Optica, Sharp mode: frames between shadow updates of nearby pixels (further away, less often). "
                        + "Higher is faster; moving lights and blocks update with more delay.");
        for (String v : List.of("1", "2", "3", "4", "6", "8", "12", "16"))
            t.put("value." + SHADOW_UPDATE + "." + v, v.equals("1") ? "Every Frame" : v + " Frames");

        return Map.copyOf(t);
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
