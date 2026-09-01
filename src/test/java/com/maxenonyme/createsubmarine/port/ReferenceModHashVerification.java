package com.maxenonyme.createsubmarine.port;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ReferenceModHashVerification {
    private static final String ENABLE_PROPERTY = "modPort.verifyReferenceModHashes";

    private ReferenceModHashVerification() {
    }

    static void assertMatches(Path path, String expected) throws Exception {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        String actual = HexFormat.of().withUpperCase().formatHex(digest.digest());
        assertEquals(expected, actual, "Referenced mod JAR hash changed");
    }
}
