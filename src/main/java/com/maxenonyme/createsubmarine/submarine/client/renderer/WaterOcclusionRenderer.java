package com.maxenonyme.createsubmarine.submarine.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11C.GL_BACK;
import static org.lwjgl.opengl.GL11C.GL_FRONT;
import static org.lwjgl.opengl.GL11C.glClearDepth;
import static org.lwjgl.opengl.GL11C.glCullFace;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;

/** Restores the upstream Sable water-occlusion renderer against Veil 1.0.0.296. */
public final class WaterOcclusionRenderer {
    public static final String CLOSE_SAMPLER_NAME = "SableCloseSampler";
    public static final String FAR_SAMPLER_NAME = "SableFarSampler";
    public static final String ENABLE_UNIFORM = "SableWaterOcclusionEnabled";
    public static final WaterOcclusionRenderer INSTANCE = new WaterOcclusionRenderer();

    private final Set<WaterOcclusionRenderRegion> regions = new LinkedHashSet<>();
    private AdvancedFbo closeBuffer;
    private AdvancedFbo farBuffer;
    private Level level;
    private static boolean enabled;

    private WaterOcclusionRenderer() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        WaterOcclusionRenderer.enabled = enabled;
    }

    @Nullable
    public WaterOcclusionRenderRegion addRegion(Collection<BlockPos> blocks) {
        if (blocks.isEmpty()) return null;
        WaterOcclusionRenderRegion region = new WaterOcclusionRenderRegion(blocks);
        regions.add(region);
        return region;
    }

    public void removeRegion(@Nullable WaterOcclusionRenderRegion region) {
        if (region == null) return;
        region.free();
        regions.remove(region);
    }

    public @Nullable AdvancedFbo getCloseBuffer() {
        return closeBuffer;
    }

    public @Nullable AdvancedFbo getFarBuffer() {
        return farBuffer;
    }

    public void preRenderTranslucent(Matrix4f modelView, Matrix4f projection) {
        updateLevel();
        if (!enabled || level == null) return;

        WaterOcclusionContainer<?> container = WaterOcclusionContainer.getContainer(level);
        boolean needed = !regions.isEmpty() && container != null;
        updateFramebuffers(needed);
        if (!needed) return;

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        closeBuffer.bind(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        glEnable(GL_DEPTH_CLAMP);
        glCullFace(GL_BACK);
        glClearDepth(container.isOccluded(cameraPos) ? 0.0 : 1.0);
        closeBuffer.clear();
        for (WaterOcclusionRenderRegion region : regions) {
            region.render(modelView, projection);
        }
        AdvancedFbo.unbind();

        farBuffer.bind(true);
        glClearDepth(1.0);
        farBuffer.clear();
        glCullFace(GL_FRONT);
        for (WaterOcclusionRenderRegion region : regions) {
            region.render(modelView, projection);
        }
        AdvancedFbo.unbind();

        glCullFace(GL_BACK);
        glDisable(GL_DEPTH_CLAMP);
        glClearDepth(1.0);
    }

    public void setupTranslucentShader(ShaderInstance shader) {
        if (!enabled) return;
        Uniform enabledUniform = shader.getUniform(ENABLE_UNIFORM);
        if (closeBuffer == null || farBuffer == null) {
            if (enabledUniform != null) enabledUniform.set(0.0F);
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        Uniform screenSize = shader.getUniform("ScreenSize");
        if (screenSize != null) screenSize.set((float) window.getWidth(), (float) window.getHeight());
        if (enabledUniform != null) enabledUniform.set(1.0F);
        shader.setSampler(CLOSE_SAMPLER_NAME, closeBuffer.getDepthTextureAttachment());
        shader.setSampler(FAR_SAMPLER_NAME, farBuffer.getDepthTextureAttachment());
    }

    private void updateLevel() {
        Level current = Minecraft.getInstance().level;
        if (current == level) return;
        level = current;
        regions.forEach(WaterOcclusionRenderRegion::free);
        regions.clear();
        updateFramebuffers(false);
    }

    private void updateFramebuffers(boolean needed) {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        if (!needed && closeBuffer != null) {
            closeBuffer.free();
            farBuffer.free();
            closeBuffer = null;
            farBuffer = null;
        }
        if (needed && (closeBuffer == null || target.width != closeBuffer.getWidth()
                || target.height != closeBuffer.getHeight())) {
            if (closeBuffer != null) {
                closeBuffer.free();
                farBuffer.free();
            }
            closeBuffer = AdvancedFbo.withSize(target.width, target.height)
                    .addColorTextureBuffer().setDepthTextureBuffer().build(true);
            farBuffer = AdvancedFbo.withSize(target.width, target.height)
                    .addColorTextureBuffer().setDepthTextureBuffer().build(true);
        }
    }
}
