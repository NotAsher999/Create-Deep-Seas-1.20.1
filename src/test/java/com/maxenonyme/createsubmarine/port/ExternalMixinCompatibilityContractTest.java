package com.maxenonyme.createsubmarine.port;

import com.maxenonyme.createsubmarine.submarine.mixin.RopeStrandHolderBehaviorMixin;
import com.maxenonyme.createsubmarine.submarine.mixin.compat.sable.SableSubLevelPocketFogMixin;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

class ExternalMixinCompatibilityContractTest {
    private static final String SIMULATED_SHA256 =
            "B9E75E12C3928D6A9F3BD16E5249A0AD74FAB239538F76BAD4DE4A58F9915DB8";
    private static final String SABLE_SHA256 =
            "0A23C66EF70AB04DAB2DF03BB5150AB6576561F446A7CED709FD819A30D3E216";

    private static final String ROPE_HOLDER =
            "dev/simulated_team/simulated/content/blocks/rope/RopeStrandHolderBehavior";
    private static final String CREATE_ROPE =
            "(Ldev/simulated_team/simulated/content/blocks/rope/RopeStrandHolderBehavior;Z)Z";
    private static final String VEC3 = "net/minecraft/world/phys/Vec3";
    private static final String CLOSER_THAN =
            "(Lnet/minecraft/core/Position;D)Z";
    private static final String CLOSER_THAN_TARGET =
            "Lnet/minecraft/world/phys/Vec3;closerThan" + CLOSER_THAN;

    private static final String SABLE_RENDER_DATA =
            "dev/ryanhcode/sable/sublevel/render/vanilla/VanillaChunkedSubLevelRenderData";
    private static final String RENDER_CHUNKED_SUB_LEVEL =
            "(Lnet/minecraft/client/renderer/RenderType;" +
            "Lnet/minecraft/client/renderer/ShaderInstance;" +
            "Lorg/joml/Matrix4f;DDD)I";
    private static final String FOG_HANDLER =
            "(Lnet/minecraft/client/renderer/RenderType;" +
            "Lnet/minecraft/client/renderer/ShaderInstance;" +
            "Lorg/joml/Matrix4f;DDD" +
            Type.getDescriptor(CallbackInfoReturnable.class) + ")V";

    @Test
    void referencedSimulatedArtifactStillContainsTheRemappedRopeInvocation() throws Exception {
        Path jar = Path.of(System.getProperty("createDeepSeas.simulatedJar"));
        ReferenceModHashVerification.assertMatches(jar, SIMULATED_SHA256);

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(ROPE_HOLDER + ".class");
            assertNotNull(entry, "Locked Simulated artifact lacks RopeStrandHolderBehavior");
            Set<String> invocations = new HashSet<>();
            final boolean[] foundMethod = {false};
            try (InputStream input = zip.getInputStream(entry)) {
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                            String signature, String[] exceptions) {
                        if (!"createRope".equals(name) || !CREATE_ROPE.equals(descriptor)) {
                            return null;
                        }
                        foundMethod[0] = true;
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String invokedName,
                                    String invokedDescriptor, boolean isInterface) {
                                invocations.add(owner + "." + invokedName + invokedDescriptor);
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            assertTrue(foundMethod[0], "Simulated createRope ABI changed");
            assertTrue(invocations.contains(VEC3 + ".closerThan" + CLOSER_THAN),
                    "Simulated createRope no longer invokes Vec3.closerThan(Position, double)");
        }
    }

    @Test
    void ropeRedirectExplicitlyRemapsTheMinecraftInvocation() throws IOException {
        final boolean[] foundRedirect = {false};
        final boolean[] remap = {false};
        final String[] target = {null};

        readCompiledClass(RopeStrandHolderBehaviorMixin.class, new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!"createsubmarine$redirectCloserThan".equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                        if (!Type.getDescriptor(Redirect.class).equals(annotationDescriptor)) {
                            return null;
                        }
                        foundRedirect[0] = true;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                                if (!"at".equals(name) || !Type.getDescriptor(At.class).equals(descriptor)) {
                                    return null;
                                }
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String name, Object value) {
                                        if ("target".equals(name)) {
                                            target[0] = (String) value;
                                        } else if ("remap".equals(name)) {
                                            remap[0] = (Boolean) value;
                                        }
                                    }
                                };
                            }
                        };
                    }
                };
            }
        });

        assertTrue(foundRedirect[0], "Missing steel-cable Vec3 redirect");
        assertEquals(CLOSER_THAN_TARGET, target[0]);
        assertTrue(remap[0], "Minecraft invocation inside an external remap=false Mixin must remap");
    }

    @Test
    void referencedSableRendererAndFogCallbacksAgreeOnTheIntegerReturnContract() throws Exception {
        Path jar = Path.of(System.getProperty("createDeepSeas.sableJar"));
        ReferenceModHashVerification.assertMatches(jar, SABLE_SHA256);
        assertJarMethod(jar, SABLE_RENDER_DATA, "renderChunkedSubLevel", RENDER_CHUNKED_SUB_LEVEL);

        Set<String> handlers = new HashSet<>();
        readCompiledClass(SableSubLevelPocketFogMixin.class, new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (name.equals("createsubmarine$defogBegin") || name.equals("createsubmarine$defogEnd")) {
                    handlers.add(name + descriptor);
                }
                return null;
            }
        });
        assertEquals(Set.of(
                "createsubmarine$defogBegin" + FOG_HANDLER,
                "createsubmarine$defogEnd" + FOG_HANDLER), handlers);
    }

    private static void assertJarMethod(Path jar, String className, String methodName,
            String methodDescriptor) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(className + ".class");
            assertNotNull(entry, () -> "Missing dependency class " + className);
            Set<String> methods = new HashSet<>();
            try (InputStream input = zip.getInputStream(entry)) {
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                            String signature, String[] exceptions) {
                        methods.add(name + descriptor);
                        return null;
                    }
                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            assertTrue(methods.contains(methodName + methodDescriptor),
                    () -> className + " lacks " + methodName + methodDescriptor);
        }
    }

    private static void readCompiledClass(Class<?> type, ClassVisitor visitor) throws IOException {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream input = type.getResourceAsStream(resource)) {
            assertNotNull(input, () -> "Missing compiled class " + resource);
            new ClassReader(input).accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG |
                    ClassReader.SKIP_FRAMES);
        }
    }

}
