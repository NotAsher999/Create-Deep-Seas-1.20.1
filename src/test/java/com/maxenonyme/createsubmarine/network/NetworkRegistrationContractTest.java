package com.maxenonyme.createsubmarine.network;

import com.maxenonyme.createsubmarine.submarine.network.CameraShakePayload;
import com.maxenonyme.createsubmarine.submarine.network.ElectrolyzerTogglePayload;
import com.maxenonyme.createsubmarine.submarine.network.HullConfigEditPayload;
import com.maxenonyme.createsubmarine.submarine.network.HullConfigSyncPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubCrackPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubLevelBoundsPayload;
import com.maxenonyme.createsubmarine.submarine.network.SubmarineNetwork;
import net.minecraftforge.network.NetworkDirection;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NetworkRegistrationContractTest {
    private static final String OWNER = "com/maxenonyme/createsubmarine/submarine/network/SubmarineNetwork";
    private static final String DIRECTION_OWNER = Type.getInternalName(NetworkDirection.class);

    @Test
    void packetOrderAndDirectionsRemainExplicitAndStable() throws IOException {
        List<Registration> registrations = new ArrayList<>();
        try (InputStream input = SubmarineNetwork.class.getResourceAsStream("SubmarineNetwork.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (!name.equals("register") || !descriptor.equals("()V")) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        private String packetClass;
                        private String direction;

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Type type && type.getSort() == Type.OBJECT) {
                                packetClass = type.getClassName();
                            }
                        }

                        @Override
                        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                            if (opcode == Opcodes.GETSTATIC && owner.equals(DIRECTION_OWNER)) {
                                direction = name;
                            }
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                                    boolean isInterface) {
                            if (opcode == Opcodes.INVOKESTATIC && owner.equals(OWNER) && name.equals("register")
                                    && descriptor.startsWith("(Ljava/lang/Class;")) {
                                registrations.add(new Registration(packetClass, direction));
                                packetClass = null;
                                direction = null;
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }

        assertEquals(List.of(
                new Registration(SubLevelBoundsPayload.class.getName(), "PLAY_TO_CLIENT"),
                new Registration(SubCrackPayload.class.getName(), "PLAY_TO_CLIENT"),
                new Registration(ElectrolyzerTogglePayload.class.getName(), "PLAY_TO_SERVER"),
                new Registration(HullConfigSyncPayload.class.getName(), "PLAY_TO_CLIENT"),
                new Registration(HullConfigEditPayload.class.getName(), "PLAY_TO_SERVER"),
                new Registration(CameraShakePayload.class.getName(), "PLAY_TO_CLIENT"),
                new Registration("com.maxenonyme.AbyssDimension.network.StruggleSharkPayload",
                        "PLAY_TO_SERVER")), registrations);
    }

    private record Registration(String packetClass, String direction) {
    }
}
