package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Single source of truth for the owner "household" notion used by the {@code POST /owners}
 * pipeline: two owners belong to the same household when they share a last name and an address,
 * compared case-insensitively with collapsed whitespace. Centralised here (rather than inlined
 * in {@link CheckOwnerHouseholdUnique} and {@link AssignHousehold}) so the uniqueness guard and
 * the id assignment cannot silently disagree about who belongs together.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /** Whether the create request names the same household as an existing owner. */
    static boolean matches(OwnerFieldsDto request, Owner existing) {
        return normalize(request.getLastName()).equals(normalize(existing.getLastName()))
                && normalize(request.getAddress()).equals(normalize(existing.getAddress()));
    }

    /**
     * A stable identifier for the household the request names: the first 8 bytes of
     * SHA-256 over the normalised last name and address, upper-cased hex. Deterministic, so
     * every member of the same household derives the same id.
     */
    static String deriveId(OwnerFieldsDto request) {
        String key = normalize(request.getLastName()) + "\n" + normalize(request.getAddress());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** The comparison form: trimmed, internal whitespace collapsed, lower-cased. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
