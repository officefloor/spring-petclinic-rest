package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * The deterministic householdId shared by {@link CheckUniqueOwnerIdentity} (which needs the id a
 * request would receive to detect household duplicates and build its identity key) and
 * {@link AssignOwnerHousehold} (which assigns it). The id is the first 12 hex characters of
 * SHA-256 over {@code <V2>|<normalizedLastName>|<postcode>} (the fixed version-2 tag mixed in), so owners with the same last name and
 * postcode share it automatically without any owner having to opt in: the household is keyed on
 * {@code (lastName, postcode)}, not on an existing member's id. {@code sharesHousehold} no longer
 * creates the link; it only bypasses the duplicate block for a declared household member.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /** The householdId a request would be assigned, from its last name and postcode. */
    static String householdId(OwnerFieldsDto request) {
        return compute(request.getLastName(), request.getPostcode());
    }

    /** The householdId an existing owner belongs to, computed the same way from its own fields. */
    static String householdId(Owner owner) {
        return compute(owner.getLastName(), owner.getPostcode());
    }

    /** First 12 hex characters of SHA-256 over {@code <V2>|<normalizedLastName>|<postcode>}. */
    private static String compute(String lastName, String postcode) {
        String key = Localities.IDENTITY_VERSION + "|" + normalizeName(lastName) + "|" + safe(postcode);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /** Lower-case and collapse runs of whitespace so comparison is case- and whitespace-insensitive. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
