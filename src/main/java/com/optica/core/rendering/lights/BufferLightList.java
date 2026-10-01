// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.rendering.lights;

import org.joml.Vector3d;

import com.optica.api.gpu.buffers.heap.IGpuBufferHeap;
import com.optica.api.gpu.buffers.heap.MemoryView;
import com.optica.api.gpu.systems.IRenderSystem;
import com.optica.core.iris.pipeline.buffer.IBufferHolder;
import com.optica.core.rendering.SectionManager;
import com.optica.core.rendering.WorldOrigin;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class BufferLightList extends AbstractLightList {
    // 12 floats per light struct (4 bytes per float)
    // position (vec3f) + block id, color (vec3f) + intensity, attenuation (vec2f) + falloff + radius in blocks
    private static final int LIGHT_DATA_SIZE = 12;
    private static final int LIGHT_BYTE_SIZE = LIGHT_DATA_SIZE * 4;

    private final IGpuBufferHeap listHeap;
    private final MemoryView listView;

    private final IGpuBufferHeap mappingHeap;
    private final MemoryView mappingView;

    // Optica: spatial light bins for the BASIC lighting mode, see LightBins.
    private final LightBins bins;
    private final IGpuBufferHeap binsHeap;
    private final MemoryView binsView;

    public BufferLightList(
            SectionManager sectionManager,
            int maxLights,
            Supplier<WorldOrigin> worldOriginSupplier,
            LightMerging merging
    ) {
        super(sectionManager, maxLights, worldOriginSupplier, merging);

        this.listHeap = IRenderSystem.getDevice()
                .ph$createBufferHeap(
                        () -> "Photonics Light List",
                        (long) maxLights * LIGHT_BYTE_SIZE,
                        0
                );

        this.listView = listHeap.allocateOrThrow(listHeap.capacity());


        this.mappingHeap = IRenderSystem.getDevice()
                .ph$createBufferHeap(
                        () -> "Photonics Light Mapping",
                        (long) maxLights * 4,
                        0
                );

        this.mappingView = mappingHeap.allocateOrThrow(mappingHeap.capacity());

        this.bins = new LightBins(maxLights);
        this.binsHeap = IRenderSystem.getDevice()
                .ph$createBufferHeap(
                        () -> "Optica Light Bins",
                        LightBins.byteSize(maxLights),
                        0
                );

        this.binsView = binsHeap.allocateOrThrow(binsHeap.capacity());
    }

    @Override
    protected void storeBins(LightList lights, WorldOrigin origin, Vector3d cameraPosition) {
        bins.build(binsView.buffer(), lights, origin, cameraPosition);
    }

    @Override
    protected void storeLight(int index, Vector4f[] light) {
        ByteBuffer buffer = listView.buffer().position(index * LIGHT_BYTE_SIZE);

        for (var vec : light) {
            buffer.putFloat(vec.x);
            buffer.putFloat(vec.y);
            buffer.putFloat(vec.z);
            buffer.putFloat(vec.w);
        }
    }

    @Override
    protected void storeMapping(int beforeIndex, int afterIndex) {
        mappingView.buffer().putInt(beforeIndex * 4, afterIndex);
    }

    @Override
    protected void clearMapping() {
        for (int i = 0; i < mostRecentLights.size(); i++) {
            mappingView.buffer().putInt(i * 4, i);
        }

        mappingView.upload();
    }

    @Override
    protected void prepareUpload() {
        listView.upload();
        mappingView.upload();
        binsView.upload();
    }

    @Override
    protected void upload() {
        listHeap.upload();
        mappingHeap.upload();
        binsHeap.upload();
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap(
                "ph_light_list",
                () -> listHeap
        );

        buffers.addDefaultBufferHeap(
                "ph_light_mapping",
                () -> mappingHeap
        );

        buffers.addDefaultBufferHeap(
                "ph_light_bins",
                () -> binsHeap
        );
    }

    @Override
    public void close() {
        super.close();

        listHeap.close();
        mappingHeap.close();
        binsHeap.close();
    }
}
