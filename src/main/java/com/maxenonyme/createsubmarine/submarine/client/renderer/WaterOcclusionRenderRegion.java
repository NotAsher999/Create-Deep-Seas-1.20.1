package com.maxenonyme.createsubmarine.submarine.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.render.region.SimpleCulledRenderRegionBuilder;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3i;

import java.util.Collection;

/** 1.20.1 port of Sable's excluded client water-occlusion mesh. */
public final class WaterOcclusionRenderRegion {
    private Collection<BlockPos> unbuiltData;
    private VertexBuffer buffer;
    private Vec3 origin;

    public WaterOcclusionRenderRegion(Collection<BlockPos> blocks) {
        this.unbuiltData = blocks;
    }

    public void render(Matrix4f modelView, Matrix4f projectionMatrix) {
        if (buffer == null) {
            build();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        SubLevel subLevel = Sable.HELPER.getContaining(client.level, origin);
        Vec3 globalOrigin = origin;
        Quaternionf globalOrientation = new Quaternionf();
        if (subLevel instanceof ClientSubLevel clientSubLevel) {
            Pose3dc renderPose = clientSubLevel.renderPose(client.getFrameTime());
            globalOrigin = renderPose.transformPosition(globalOrigin);
            globalOrientation.set(renderPose.orientation());
        }

        Vec3 relativePos = globalOrigin.subtract(client.gameRenderer.getMainCamera().getPosition());
        Matrix4f transformedModelView = new Matrix4f(modelView)
                .setTranslation(0.0F, 0.0F, 0.0F)
                .translate((float) relativePos.x, (float) relativePos.y, (float) relativePos.z)
                .rotate(globalOrientation);

        buffer.bind();
        buffer.drawWithShader(transformedModelView, projectionMatrix, shader);
        VertexBuffer.unbind();
    }

    private void build() {
        BlockPos firstBlock = unbuiltData.stream().findFirst().orElseThrow();
        Vector3i min = new Vector3i(firstBlock.getX(), firstBlock.getY(), firstBlock.getZ());
        Vector3i max = new Vector3i(min);
        Vector3i current = new Vector3i();
        for (BlockPos block : unbuiltData) {
            current.set(block.getX(), block.getY(), block.getZ());
            min.min(current);
            max.max(current);
        }

        int gridSize = Math.max(max.x() - min.x() + 1,
                Math.max(max.y() - min.y() + 1, max.z() - min.z() + 1));
        BlockPos originBlock = new BlockPos(min.x(), min.y(), min.z());
        origin = Vec3.atLowerCornerOf(originBlock);

        SimpleCulledRenderRegionBuilder mesh = new SimpleCulledRenderRegionBuilder(gridSize);
        for (BlockPos block : unbuiltData) {
            mesh.add(block.getX() - originBlock.getX(), block.getY() - originBlock.getY(),
                    block.getZ() - originBlock.getZ());
        }
        mesh.buildNoGreedy();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        mesh.render(new Matrix4f(), builder);
        buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.end());
        VertexBuffer.unbind();
        unbuiltData = null;
    }

    public void free() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
    }
}
