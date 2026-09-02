package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CheckDigit;
import org.springframework.samples.petclinic.util.FiscalYear;

/**
 * Builds an owner's {@code memberId} in the format {@code '<REGION><FY><HASH8><CHK>'}: the region
 * code derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, then the fixed city
 * fallback, else {@code UNKNOWN}), the two-digit fiscal year of the registration date, the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone concatenated with the
 * last name, and a single Luhn check digit over the digits of {@code '<REGION><FY><HASH8>'}.
 */
final class CustomerCode {

    /** Fixed city-to-region fallback, used only when the postcode identifies no region. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CustomerCode() {
    }

    static String of(Owner owner, Collection<Owner> existing) {
        String core = region(owner) + fiscalYear(owner) + hash8(owner.getTelephone() + owner.getLastName());
        return dedupe(core + CheckDigit.luhn(core), existing);
    }

    /** The two-digit fiscal year of the owner's registration date. */
    private static String fiscalYear(Owner owner) {
        return String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
    }

    /** Appends {@code '-<n>'} (smallest {@code n >= 2}) when {@code base} collides with an existing code. */
    private static String dedupe(String base, Collection<Owner> existing) {
        Set<String> taken = existing.stream().map(Owner::getCustomerCode)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        String candidate = base;
        for (int n = 2; taken.contains(candidate); n++) {
            candidate = base + "-" + n;
        }
        return candidate;
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
        String plain = byPostcode != null ? byPostcode : CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
        return "V2" + plain;
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
