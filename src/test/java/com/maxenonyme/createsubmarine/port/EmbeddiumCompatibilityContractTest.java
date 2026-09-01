package com.maxenonyme.createsubmarine.port;

import com.maxenonyme.createsubmarine.submarine.mixin.compat.SodiumBlockRenderContextAccessor;
import com.maxenonyme.createsubmarine.submarine.mixin.compat.SodiumBlockRendererMixin;
import com.maxenonyme.createsubmarine.submarine.mixin.compat.SodiumChunkRendererMixin;
import com.maxenonyme.createsubmarine.submarine.mixin.compat.SodiumFluidRendererMixin;
import com.maxenonyme.createsubmarine.submarine.mixin.compat.SodiumShaderLoaderMixin;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddiumCompatibilityContractTest {
    private static final String EXPECTED_SHA256 =
            "EED3D1325F2ACC2FD4E69BB495E5CCB91D962126AC5330F0582EBC2A3DAF47FB";

    private static final String SHADER_LOADER =
            "me/jellysquid/mods/sodium/client/gl/shader/ShaderLoader";
    private static final String CHUNK_RENDERER =
            "me/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer";
    private static final String TERRAIN_PASS =
            "me/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass";
    private static final String GL_OBJECT =
            "me/jellysquid/mods/sodium/client/gl/GlObject";
    private static final String FLUID_RENDERER =
            "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer";
    private static final String BLOCK_RENDERER =
            "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer";
    private static final String BLOCK_CONTEXT =
            "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext";

    private static final String SHADER_SOURCE =
            "getShaderSource(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/String;";
    private static final String CHUNK_BEGIN =
            "begin(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V";
    private static final String FLUID_RENDER =
            "render(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V";
    private static final String BLOCK_RENDER =
            "renderModel(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V";

    @Test
    void referencedEmbeddiumArtifactStillExposesThePortedTargets() throws Exception {
        Path jar = Path.of(System.getProperty("createDeepSeas.embeddiumJar"));
        ReferenceModHashVerification.assertMatches(jar, EXPECTED_SHA256);

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            String metadata = readText(zip, "META-INF/mods.toml");
            assertTrue(metadata.contains("modId=\"embeddium\""));
            assertTrue(metadata.contains("version=\"0.3.31+mc1.20.1\""));

            assertMethod(zip, SHADER_LOADER, SHADER_SOURCE);
            assertMethod(zip, CHUNK_RENDERER, CHUNK_BEGIN);
            assertField(zip, CHUNK_RENDERER,
                    "activeProgram:Lme/jellysquid/mods/sodium/client/gl/shader/GlProgram;");
            assertMethod(zip, GL_OBJECT, "handle()I");
            assertMethod(zip, TERRAIN_PASS, "isReverseOrder()Z");
            assertMethod(zip, FLUID_RENDERER, FLUID_RENDER);
            assertMethod(zip, BLOCK_RENDERER, BLOCK_RENDER);
            assertMethod(zip, BLOCK_CONTEXT, "pos()Lnet/minecraft/core/BlockPos;");
        }
    }

    @Test
    void mixinsDeclareTheExactLockedEmbeddiumContracts() throws IOException {
        assertMixin(SodiumShaderLoaderMixin.class, SHADER_LOADER.replace('/', '.'), SHADER_SOURCE);
        assertMixin(SodiumChunkRendererMixin.class, CHUNK_RENDERER.replace('/', '.'), CHUNK_BEGIN);
        assertMixin(SodiumFluidRendererMixin.class, FLUID_RENDERER.replace('/', '.'), FLUID_RENDER);
        assertMixin(SodiumBlockRendererMixin.class, BLOCK_RENDERER.replace('/', '.'), BLOCK_RENDER);

        assertEquals(Set.of(BLOCK_CONTEXT.replace('/', '.')),
                readMixinTargets(SodiumBlockRenderContextAccessor.class));
    }

    private static void assertMixin(Class<?> mixinClass, String target, String injectedMethod) throws IOException {
        assertEquals(Set.of(target), readMixinTargets(mixinClass));

        Set<String> injections = readInjectionTargets(mixinClass);
        assertTrue(injections.contains(injectedMethod),
                () -> mixinClass.getSimpleName() + " does not inject " + injectedMethod);
    }

    private static Set<String> readMixinTargets(Class<?> mixinClass) throws IOException {
        Set<String> targets = new HashSet<>();
        String resource = "/" + mixinClass.getName().replace('.', '/') + ".class";
        try (InputStream input = mixinClass.getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Missing compiled mixin " + resource);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (!Type.getDescriptor(Mixin.class).equals(descriptor)) {
                        return null;
                    }
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitArray(String annotationName) {
                            if (!"targets".equals(annotationName)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public void visit(String name, Object value) {
                                    targets.add((String) value);
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return targets;
    }

    private static Set<String> readInjectionTargets(Class<?> mixinClass) throws IOException {
        Set<String> injections = new HashSet<>();
        String resource = "/" + mixinClass.getName().replace('.', '/') + ".class";
        try (InputStream input = mixinClass.getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Missing compiled mixin " + resource);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                            if (!Type.getDescriptor(Inject.class).equals(annotationDescriptor)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public AnnotationVisitor visitArray(String annotationName) {
                                    if (!"method".equals(annotationName)) {
                                        return null;
                                    }
                                    return new AnnotationVisitor(Opcodes.ASM9) {
                                        @Override
                                        public void visit(String name, Object value) {
                                            injections.add((String) value);
                                        }
                                    };
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return injections;
    }

    private static void assertMethod(ZipFile zip, String className, String expected) throws IOException {
        ClassContract contract = readContract(zip, className);
        assertTrue(contract.methods.contains(expected), () -> className + " lacks " + expected);
    }

    private static void assertField(ZipFile zip, String className, String expected) throws IOException {
        ClassContract contract = readContract(zip, className);
        assertTrue(contract.fields.contains(expected), () -> className + " lacks " + expected);
    }

    private static ClassContract readContract(ZipFile zip, String className) throws IOException {
        ZipEntry entry = zip.getEntry(className + ".class");
        assertNotNull(entry, () -> "Missing Embeddium class " + className);
        Set<String> methods = new HashSet<>();
        Set<String> fields = new HashSet<>();
        try (InputStream input = zip.getInputStream(entry)) {
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    methods.add(name + descriptor);
                    return null;
                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor,
                        String signature, Object value) {
                    fields.add(name + ":" + descriptor);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return new ClassContract(methods, fields);
    }

    private static String readText(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        assertNotNull(entry, () -> "Missing " + name);
        try (InputStream input = zip.getInputStream(entry)) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private record ClassContract(Set<String> methods, Set<String> fields) {
    }
}
