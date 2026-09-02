package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code customerCode} in the format {@code '<REGION>-<HASH8>'}: the region code
 * derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, then the fixed city
 * fallback, else {@code UNKNOWN}) and the first eight upper-case hex characters of SHA-256 over the
 * normalized telephone concatenated with the last name. No sequence numbers are used.
 */
final class CustomerCode {

    /** Fixed city-to-region fallback, used only when the postcode identifies no region. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CustomerCode() {
    }

    static String of(Owner owner, Collection<Owner> existing) {
        return region(owner) + "-" + hash8(owner.getTelephone() + owner.getLastName());
    }

    private static String region(Owner owner) {
        String postcode = owner.getPostcode();
        String byPostcode = postcode == null || !postcode.matches("[0-9]{4}") ? null
            : switch (Integer.parseInt(postcode) / 100) {
                case 20 -> "NSW";
                case 30 -> "VIC";
                case 40 -> "QLD";
                default -> null;
            };
        return byPostcode != null ? byPostcode : CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String hash8(String value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256(value)) {
            sb.append(String.format("%02X", b));
        }
        return sb.substring(0, 8);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
