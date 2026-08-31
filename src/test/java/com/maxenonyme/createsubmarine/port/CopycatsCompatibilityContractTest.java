package com.maxenonyme.createsubmarine.port;

import com.maxenonyme.createsubmarine.submarine.mixin.compat.copycat.CopycatWrenchMixin;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopycatsCompatibilityContractTest {
    private static final String EXPECTED_SHA256 =
            "7D684E6A829FCACF5AB94D20044CAD0E7EB5376CBE02D77954FC41A2D264511F";
    private static final String TARGET =
            "com/copycatsplus/copycats/foundation/copycat/ICopycatBlock";
    private static final String ON_WRENCHED =
            "onWrenched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;";

    @Test
    void lockedCopycatsArtifactStillExposesThePortedInterfaceMethod() throws Exception {
        Path jar = Path.of(System.getProperty("createDeepSeas.copycatsJar"));
        assertEquals(EXPECTED_SHA256, sha256(jar));

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(TARGET + ".class");
            assertNotNull(entry, "Locked Copycats artifact lacks ICopycatBlock");
            Set<String> methods = new HashSet<>();
            final boolean[] isInterface = {false};
            try (InputStream input = zip.getInputStream(entry)) {
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(int version, int access, String name, String signature,
                            String superName, String[] interfaces) {
                        isInterface[0] = (access & Opcodes.ACC_INTERFACE) != 0;
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                            String signature, String[] exceptions) {
                        methods.add(name + descriptor);
                        return null;
                    }
                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            assertTrue(isInterface[0], "ICopycatBlock must remain an interface");
            assertTrue(methods.contains(ON_WRENCHED), "ICopycatBlock onWrenched ABI changed");
        }
    }

    @Test
    void copycatMixinInterceptsTheCanonicalCreateWrenchCall() throws IOException {
        assertTrue(!CopycatWrenchMixin.class.isInterface(),
                "Mixin 0.8.5 cannot inject into an interface Mixin");

        Set<String> targets = new HashSet<>();
        Set<String> injections = new HashSet<>();
        String resource = "/" + CopycatWrenchMixin.class.getName().replace('.', '/') + ".class";
        try (InputStream input = CopycatWrenchMixin.class.getResourceAsStream(resource)) {
            assertNotNull(input, "Missing compiled Copycats Mixin");
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (!Type.getDescriptor(Mixin.class).equals(descriptor)) {
                        return null;
                    }
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitArray(String name) {
                            if (!"targets".equals(name) && !"value".equals(name)) {
                                return null;
                            }
                            return new AnnotationVisitor(Opcodes.ASM9) {
                                @Override
                                public void visit(String name, Object value) {
                                    if (value instanceof Type type) {
                                        targets.add(type.getClassName());
                                    } else {
                                        targets.add((String) value);
                                    }
                                }
                            };
                        }
                    };
                }

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
                                public AnnotationVisitor visitArray(String name) {
                                    if (!"method".equals(name)) {
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

        assertEquals(Set.of(WrenchItem.class.getName()), targets);
        assertTrue(injections.contains("useOn"));
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().withUpperCase().formatHex(digest.digest());
    }
}
