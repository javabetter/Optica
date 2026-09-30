# Testing Optica headless (cloud container)

The container has no GPU, but Mesa llvmpipe provides OpenGL 4.5 core (4.6 with
`MESA_GL_VERSION_OVERRIDE=4.6`), which is enough for Minecraft, Iris and Optica.

```bash
apt-get install -y openjdk-25-jdk-headless mesa-utils xdotool imagemagick   # xvfb is preinstalled
Xvfb :99 -screen 0 1280x720x24 +extension GLX &
export DISPLAY=:99 JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
./gradlew runClient --args="--quickPlaySingleplayer <save folder>"   # save folder name without spaces
import -window root shot.png                                          # screenshot
xdotool key t; xdotool type "/time set midnight"; xdotool key Return   # chat commands
```

Notes:
- Shader packs go in `run/shaderpacks/` (git-ignored; never commit third-party packs). Select one
  with `shaderPack=` in `run/config/iris.properties`. Pack options go in
  `run/shaderpacks/<pack name>.txt` (e.g. `PHOTONICS_LIGHTING_MODE=2`). Press `R` in game to
  reload shaders and `K` to toggle them.
- Stop the client and wait for the JVM to exit *before* editing `run/options.txt`; it is saved on
  shutdown. Set `graphicsPreset:"custom"` or the preset overrides `renderDistance`.
- Photon does not run on llvmpipe (`deferred4_a` needs 36 KB of compute shared memory; the
  limit is 32 KB). Euphoria Patches and BSL work.
- No audio device and no Mojang auth: OpenAL, narrator and `sessionserver` errors in the log are
  expected.
