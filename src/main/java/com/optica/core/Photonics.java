// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core;

import com.optica.core.config.PhConfig;
import com.optica.core.config.PhConfigWatchThread;
import com.vdurmont.semver4j.Semver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

public class Photonics {
    public static final Logger LOGGER = LoggerFactory.getLogger("Optica");

    /**
     * The Photonics shader API version Optica implements. Shader packs and patches see this
     * (as {@code PHOTONICS_VERSION} and in {@code patch.json}'s {@code supportedVersions}),
     * not Optica's own mod version, so packs written for Photonics keep enabling themselves.
     */
    public static final Semver PHOTONICS_API_VERSION = new Semver("0.4.0");

    private static Semver modVersion;
    private static Semver version = PHOTONICS_API_VERSION;
    private static boolean isDevEnvironment;
    private static Path assets;

    public static void init(
            Semver opticaVersion,
            boolean isDevelopmentEnvironment,
            Path assetsPath
    ) throws URISyntaxException {
        modVersion = opticaVersion;
        isDevEnvironment = isDevelopmentEnvironment;
        assets = assetsPath;

        PhConfig.reloadConfig();
        PhConfigWatchThread.INSTANCE.start();
    }

    /** The Photonics API version Optica is compatible with; see {@link #PHOTONICS_API_VERSION}. */
    public static Semver getVersion() {
        return version;
    }

    public static Semver getModVersion() {
        return modVersion;
    }

    public static String getVersionString() {
        final String major = version.getMajor() != null ? version.getMajor().toString() : "0";
        final String minor = version.getMinor() != null ? version.getMinor().toString() : "00";
        final String patch = version.getPatch() != null ? version.getPatch().toString() : "00";

        return StringUtils.stripStart(
                major +
                        StringUtils.leftPad(minor, 2, '0') +
                        StringUtils.leftPad(patch, 2, '0'),
                "0"
        );
    }

    public static boolean isDevEnvironment() {
        return isDevEnvironment;
    }

    public static Path getAssetsPath() {
        return assets;
    }
}
