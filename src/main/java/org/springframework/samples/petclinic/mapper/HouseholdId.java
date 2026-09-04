package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives the stable household identifier shared by owners at the same household,
 * i.e. having the same last name and address (compared case-insensitively with
 * collapsed whitespace). The value is a pure function of those two fields, so every
 * owner in one household resolves to the same id without any stored state.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String address) {
        String key = normalize(lastName) + "|" + normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("H-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
