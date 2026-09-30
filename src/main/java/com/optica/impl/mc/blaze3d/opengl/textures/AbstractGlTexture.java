// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.impl.mc.blaze3d.opengl.textures;

import com.optica.api.Disposable;
import com.optica.api.gpu.textures.ITextureFormat;
import com.optica.api.gpu.textures.TextureUsage;
import com.optica.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import com.mojang.blaze3d.opengl.GlStateManager;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL45C;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.jetbrains.annotations.NonNls;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AbstractGlTexture<D> implements Disposable, IGlTexture {
    private final boolean hasUserLabel;
    private @NonNls String label;

    @TextureUsage
    protected final int usage;
    protected final InternalTextureFormat textureFormat;
    protected final int mipLevels;

    private boolean closed = false;
    private int handle = 0;

    protected D size;

    public AbstractGlTexture(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            D size,
            int mipLevels
    ) {
        this.hasUserLabel = label != null;
        this.label = label == null ? null : label.get();
        this.usage = usage;
        this.textureFormat = (InternalTextureFormat) (Object) textureFormat;
        this.mipLevels = mipLevels;

        this.size = copySize(size);
        createTextureObject();
    }

    private void createTextureObject() {
        int handle = GlStateManager._genTexture();
        initTexture(handle);
        initDefaultSamplerState(handle);

        this.handle = handle;

        if (!hasUserLabel) label = String.valueOf(handle);
        GlDebugLabelExt.getInstance().applyLabel(this);
    }

    protected abstract void initTexture(int handle);

    /**
     * Optica: gives the texture explicit NEAREST filtering and edge clamping. Photonics binds most of its
     * render targets without a sampler object, so the texture's own state is used; GL's default min filter
     * (NEAREST_MIPMAP_LINEAR) makes integer-format textures (rgba32ui frag data, reservoirs, ...) incomplete,
     * and incomplete textures read back as zero on spec-compliant drivers such as Mesa.
     */
    private static void initDefaultSamplerState(int handle) {
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
        GL45C.glTextureParameteri(handle, GL12C.GL_TEXTURE_WRAP_R, GL12C.GL_CLAMP_TO_EDGE);
    }

    protected abstract D copySize(D value);

    @Override
    public int handle() {
        return handle;
    }

    public String ph$label() {
        return label;
    }

    @TextureUsage
    public int ph$usage() {
        return usage;
    }

    public int ph$mipLevels() {
        return mipLevels;
    }

    public ITextureFormat ph$format() {
        return (ITextureFormat) (Object) textureFormat;
    }

    public void ph$resize(D newSize) {
        if (closed) throw new IllegalStateException("closed");
        if (size.equals(newSize)) return;

        this.size = copySize(newSize);

        destroyTexture();
        createTextureObject();
    }

    public boolean ph$isClosed() {
        return closed;
    }

    private void destroyTexture() {
        if (handle != 0) {
            GlStateManager._deleteTexture(handle);
            handle = 0;
        }
    }

    @Override
    public void close() {
        if (closed) return;

        destroyTexture();
        closed = true;
    }
}
