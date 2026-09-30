// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config;

import com.optica.api.mc.world.level.IBlock;
import com.optica.core.config.lights.LightDefines;
import com.optica.core.config.lights.LightGroup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The class that represents Photonics's config
 */
public class PhStorage {
    private static final LinkedHashMap<String, LightGroup> EMPTY_LIGHTS = new LinkedHashMap<>(0);

    public boolean multiThreadingEnabled = true;
    public LightDefines defines = LightDefines.EMPTY;
    public LinkedHashMap<String, LightGroup> lights = EMPTY_LIGHTS;

    /**
     * Overrides the values in {@link #lights}
     */
    public Map<IBlock, Boolean> raytracedLights = Map.of();
}
