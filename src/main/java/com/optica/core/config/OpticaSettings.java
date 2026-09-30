package com.optica.core.config;

import com.optica.api.ModLoader;
import com.optica.core.Photonics;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Optica's own settings, stored in {@code config/optica.properties}. They are read every time a shader
 * pack's properties are loaded, so a change takes effect on the next shader reload (the R key).
 *
 * <p>Shader packs choose the lighting mode themselves (Euphoria Patches offers BASIC and ReSTIR), so
 * Optica-only modes such as {@code cached} are selected here and override the pack's choice.
 */
public final class OpticaSettings {
    private static final String FILE_NAME = "optica.properties";

    private static final String DEFAULT_FILE = """
            # Optica settings. Press R in game (reload shaders) after changing this file.

            # Lighting mode.
            #   pack   - use the mode the shader pack selects (BASIC or ReSTIR in Euphoria Patches)
            #   cached - lighting is computed per block face in the world and reused for a while,
            #            refreshed in the background. Very cheap per frame; changes to lights and
            #            blocks appear with a delay of up to cacheRefreshSeconds. Entities do not
            #            cast raytraced shadows in this mode.
            lightingMode=pack

            # cached mode: seconds to refresh all cached lighting once (0.1 - 30).
            cacheRefreshSeconds=1.0

            # cached mode: lighting samples per block face edge near the camera (1, 2, 4 or 8).
            # 4 gives shadows at quarter-block resolution. Detail drops with distance.
            cacheDetail=4

            # cached mode: GPU memory for the cache in MB (16 - 512).
            cacheMemoryMb=64

            # cached mode: sky/GI rays per cache update (0 - 8). 0 disables GI in this mode.
            cacheGiSamples=2
            """;

    public enum LightingMode {PACK, CACHED}

    public final LightingMode lightingMode;
    public final float cacheRefreshSeconds;
    public final int cacheDetail;
    public final int cacheMemoryMb;
    public final int cacheGiSamples;

    private OpticaSettings(Properties properties) {
        lightingMode = parseMode(properties.getProperty("lightingMode", "pack"));
        cacheRefreshSeconds = clamp(parseFloat(properties, "cacheRefreshSeconds", 1.0f), 0.1f, 30.0f);
        cacheDetail = Integer.highestOneBit(clamp(parseInt(properties, "cacheDetail", 4), 1, 8));
        cacheMemoryMb = clamp(parseInt(properties, "cacheMemoryMb", 64), 16, 512);
        cacheGiSamples = clamp(parseInt(properties, "cacheGiSamples", 2), 0, 8);
    }

    public static OpticaSettings load() {
        Path path = ModLoader.getConfigDir().resolve(FILE_NAME);
        Properties properties = new Properties();

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, DEFAULT_FILE);
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            Photonics.LOGGER.warn("Could not read {}, using defaults", path, e);
        }

        return new OpticaSettings(properties);
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

    /** Cache entries are 32 bytes; the capacity is the largest power of two that fits the budget. */
    private int capacityLog2() {
        long entries = ((long) cacheMemoryMb << 20) / 32;
        return 63 - Long.numberOfLeadingZeros(entries);
    }

    private static LightingMode parseMode(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "cached" -> LightingMode.CACHED;
            case "pack" -> LightingMode.PACK;
            default -> {
                Photonics.LOGGER.warn("Unknown lightingMode '{}' in {}, using 'pack'", value, FILE_NAME);
                yield LightingMode.PACK;
            }
        };
    }

    private static float parseFloat(Properties properties, String key, float fallback) {
        try {
            return Float.parseFloat(properties.getProperty(key, Float.toString(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException e) {
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
