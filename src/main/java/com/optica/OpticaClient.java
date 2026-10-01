package com.optica;

import com.optica.core.Photonics;
import com.vdurmont.semver4j.Semver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

// Based on Photonics' PhotonicsClientFabric (https://github.com/Redi2Go/PhotonicEngine), LGPL-3.0.
public final class OpticaClient implements ClientModInitializer {
    public static final String MOD_ID = "optica";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModContainer optica = FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Optica's mod container is missing"));

        try {
            Photonics.init(
                    new Semver(optica.getMetadata().getVersion().getFriendlyString(), Semver.SemverType.LOOSE),
                    FabricLoader.getInstance().isDevelopmentEnvironment(),
                    optica.getRootPaths().getFirst().resolve("assets")
            );
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        StallWatchdog.start();

        LOGGER.info("Photonics Unofficial Port {} loaded (Photonics API {})", Photonics.getModVersion(), Photonics.getVersion());
    }
}
