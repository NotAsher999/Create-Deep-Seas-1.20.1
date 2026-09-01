package com.maxenonyme.createsubmarine.port;

import com.maxenonyme.createsubmarine.submarine.mixin.LevelRendererMixin;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WaterOcclusionRenderStageContractTest {
    private static final String CRUMBLING_BUFFER_SOURCE =
            "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()" +
            "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;";

    @Test
    void waterDepthPrepassRemainsBetweenBlockEntitiesAndTranslucentTerrain() {
        Method handler = Arrays.stream(LevelRendererMixin.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("createsubmarine$preRenderTranslucent"))
                .findFirst()
                .orElseThrow();

        Inject injection = handler.getAnnotation(Inject.class);
        assertNotNull(injection, "Water-occlusion render-stage handler lost its Mixin injection");
        assertEquals(1, injection.at().length);

        At at = injection.at()[0];
        assertEquals("INVOKE", at.value());
        assertEquals(CRUMBLING_BUFFER_SOURCE, at.target());
        assertEquals(2, at.ordinal(),
                "Forge 1.20.1 ordinal 2 is the destruction-buffer flush immediately before translucent terrain");
        assertEquals(At.Shift.AFTER, at.shift());
    }
}
