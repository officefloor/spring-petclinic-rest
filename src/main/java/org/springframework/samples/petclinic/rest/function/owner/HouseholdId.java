package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Derives an owner's {@code householdId}: a stable identifier shared by every owner with the same
 * last name and address (one household). The last name is normalized the same way as
 * {@link RejectDuplicateHousehold} (trim, collapse runs of whitespace to a single space, lower-case)
 * and the address in the normalized form ({@link AddressNormalizer}) — the same form the duplicate
 * check compares — before hashing, so owners created with {@code sharesHousehold} true, who by
 * definition match an existing owner's last name and address, derive the identical value. Computed as
 * the first 12 upper-case hex characters of SHA-256 over the normalized pair, so it is stable across
 * requests and needs no persisted column.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String address) {
        String key = normalize(lastName) + "\n" + AddressNormalizer.normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
