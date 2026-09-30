// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.meshing;

import com.optica.api.mc.Id;
import com.optica.common.iris.IrisUtil;
import com.optica.common.meshing.impl.BlockBuilderBufferSource;
import com.optica.common.meshing.impl.BlockSetBuilder;
import com.optica.common.meshing.impl.EmptyBufferSource;
import com.optica.common.meshing.impl.EmptyOutlineBufferSource;
import com.optica.common.meshing.impl.FeatureRendererExt;
import com.optica.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class McBlockRenderer {
    private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;
    private static final Set<Fluid> WHITELISTED_FLUIDS = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);

    private final RandomSource randomSource = RandomSource.create();
    private final ModelManager modelManager = Minecraft.getInstance().getModelManager();
    // 26.1 removed BlockRenderDispatcher; blocks are tesselated through ModelBlockRenderer directly.
    // No ambient occlusion and no face culling, matching the old tesselateWithoutAO(..., cull = false) call.
    private final ModelBlockRenderer modelRenderer = new ModelBlockRenderer(false, false, Minecraft.getInstance().getBlockColors());
    private final PoseStack poseStack = new PoseStack();

    private final BlockBuilderBufferSource bufferSource = new BlockBuilderBufferSource();

    private final LevelRenderState levelRenderState = new LevelRenderState();
    private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
    private final FeatureRenderDispatcher featureRenderDispatcher = new FeatureRenderDispatcher(
            submitNodeStorage,
            modelManager,
            bufferSource,
            Minecraft.getInstance().getAtlasManager(),
            EmptyOutlineBufferSource.INSTANCE,
            EmptyBufferSource.INSTANCE,
            Minecraft.getInstance().font,
            Minecraft.getInstance().gameRenderer.getGameRenderState()
    );

    private final SimpleMeshState.HashStorage hashStorage = new SimpleMeshState.HashStorage();

    public McBlockRenderer() {
        var featureRenderer = (FeatureRendererExt) featureRenderDispatcher;

        featureRenderer.setRenderShadows(false);
        featureRenderer.setRenderFlames(false);
        featureRenderer.setRenderNametags(false);
        featureRenderer.setRenderText(false);
        featureRenderer.setRenderParticles(false);
    }

    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter
    ) {
        if (blockState.getBlock() == Blocks.END_GATEWAY)
            return EmptyMeshState.INSTANCE;

        int blockId = IrisUtil.getBlockId(blockState);
        FluidState fluidState = blockState.getFluidState();

        List<BlockStateModelPart> parts;
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            parts = new ArrayList<>();

            randomSource.setSeed(blockState.getSeed(blockPos));
            modelManager.getBlockStateModelSet().get(blockState).collectParts(randomSource, parts);
        } else parts = List.of();

        if (blockState.hasBlockEntity()) return new DynamicMeshState(blockId, fluidState, parts);
        if (WHITELISTED_FLUIDS.contains(fluidState.getType())) return new DynamicMeshState(blockId, fluidState, parts);

        if (parts.isEmpty())
            return EmptyMeshState.INSTANCE;

        var meshState = new SimpleMeshState(blockState.getBlock(), blockId, parts);
        meshState.computeHash(
                hashStorage,
                blockState,
                blockPos,
                blockAndTintGetter
        );

        return meshState;
    }

    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        if (meshState == EmptyMeshState.INSTANCE) return;

        builder.useBlockId(meshState.blockId());

        FluidState fluidState = meshState.fluidState();
        if (!fluidState.isEmpty()) {
            submitFluid(
                    pos,
                    blockAndTintGetter,
                    builder,
                    blockState,
                    fluidState
            );
        }

        if (blockState.hasBlockEntity()) {
            submitBlockEntity(
                    pos,
                    blockState,
                    blockAndTintGetter,
                    builder
            );
        }

        List<BlockStateModelPart> parts = meshState.blockModel();
        if (!parts.isEmpty()) {
            submitBlock(
                    pos,
                    blockState,
                    blockAndTintGetter,
                    builder,
                    parts
            );
        }
    }

    private void submitFluid(
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder,
            BlockState blockState,
            FluidState fluidState
    ) {
        if (!WHITELISTED_FLUIDS.contains(fluidState.getType())) return;

        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(
                -(blockPos.getX() & 15),
                -(blockPos.getY() & 15),
                -(blockPos.getZ() & 15)
        );

        // Built per call so resource reloads (which replace the fluid model set) are picked up.
        new FluidRenderer(modelManager.getFluidStateModelSet())
                .tesselate(blockAndTintGetter, blockPos, layer -> (VertexConsumer) builder, blockState, fluidState);
    }

    private void submitBlock(
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder,
            List<BlockStateModelPart> parts
    ) {
        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(0f, 0f, 0f);

        VertexConsumer consumer = (VertexConsumer) builder;
        BlockQuadOutput output = consumer::putBlockBakedQuad;

        // Quads are emitted in block-local space (plus the block's random offset), like the old path.
        modelRenderer.tesselateBlock(
                output,
                0f, 0f, 0f,
                blockAndTintGetter,
                pos,
                blockState,
                new PartListModel(parts),
                blockState.getSeed(pos)
        );
    }

    private static final Set<Block> FULL_BLOCK_ENTITY_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.PLAYER_HEAD)
            .addBlock(Blocks.PLAYER_WALL_HEAD)
            .build();

    private static final Set<Block> LEVEL_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.CHEST)
            .build();

    private BlockEntity copyBlockEntity(BlockState blockState) {
        EntityBlock entityBlock = (EntityBlock) blockState.getBlock();
        return entityBlock.newBlockEntity(new BlockPos(0, 0, 0), blockState);
    }

    private BlockEntity fetchBlockEntity(BlockPos blockPos, BlockState blockState) {
        try {
            var level = Minecraft.getInstance().level;
            if (level == null || !level.isInValidBounds(blockPos)) return copyBlockEntity(blockState);

            var chunk = level.getChunkAt(blockPos);
            return chunk.getBlockEntity(blockPos);
        } catch (Exception e) {
            return copyBlockEntity(blockState);
        }
    }

    private void submitBlockEntity(
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        builder.useOffset(0f, 0f, 0f);
        bufferSource.setBlockBuilder(builder);

        levelRenderState.reset();

        try {
            BlockEntity entity = FULL_BLOCK_ENTITY_REQUIRED_FOR.contains(blockState.getBlock()) ?
                    fetchBlockEntity(blockPos, blockState) :
                    copyBlockEntity(blockState);

            if (entity == null) return;

            if (LEVEL_REQUIRED_FOR.contains(blockState.getBlock()))
                entity.setLevel((Level) blockAndTintGetter);

            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                    Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);

            if (renderer == null) return;

            var renderState = renderer.createRenderState();
            renderer.extractRenderState(entity, renderState, 0.8f, levelRenderState.cameraRenderState.pos, null);

            poseStack.pushPose();
            renderer.submit(renderState, poseStack, submitNodeStorage, levelRenderState.cameraRenderState);
            poseStack.popPose();

            featureRenderDispatcher.renderAllFeatures();
        } finally {
            bufferSource.setBlockBuilder(null);
        }
    }

    /** Replays an already-collected part list through {@link ModelBlockRenderer#tesselateBlock}. */
    private record PartListModel(List<BlockStateModelPart> parts) implements BlockStateModel {
        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            output.addAll(parts);
        }

        @Override
        public Material.Baked particleMaterial() {
            return parts.getFirst().particleMaterial();
        }

        @Override
        public int materialFlags() {
            int flags = 0;
            for (BlockStateModelPart part : parts) flags |= part.materialFlags();
            return flags;
        }
    }
}
