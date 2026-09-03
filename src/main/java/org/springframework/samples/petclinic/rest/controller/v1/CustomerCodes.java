package org.springframework.samples.petclinic.rest.controller.v1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.samples.petclinic.model.BusinessDays;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's memberId in the form {@code <REGION><FY><HASH8><CHK>}: the region derived
 * from the postcode, the 2-digit fiscal year, the first 8 upper-case hex characters of SHA-256
 * over the owner's normalized telephone and last name, and a single Luhn check digit over them.
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The {@code <REGION><FY><HASH8><CHK>} memberId for {@code owner}. */
    static String build(Owner owner, Collection<Owner> existing) {
        LocalDate registration = BusinessDays.roll(
            owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now());
        String base = region(owner)
            + String.format("%02d", FiscalYear.of(registration) % 100)
            + hash8(owner.getTelephone() + owner.getLastName());
        return deduplicate(base + CheckDigit.luhn(base), existing);
    }

    /** {@code code}, or the first {@code code-<n>} (n starting at 2) not held by an owner in {@code existing}. */
    private static String deduplicate(String code, Collection<Owner> existing) {
        Set<String> used = new HashSet<>();
        for (Owner owner : existing) {
            used.add(owner.getMemberId());
        }
        String candidate = code;
        for (int n = 2; used.contains(candidate); n++) {
            candidate = code + "-" + n;
        }
        return candidate;
    }

    /** The region code derived from {@code owner}'s postcode, falling back to its city, else {@code UNKNOWN}. */
    public static String region(Owner owner) {
        String prefix = owner.getPostcode() != null && owner.getPostcode().matches("[0-9]{4}")
            ? owner.getPostcode().substring(0, 2) : "";
        return switch (prefix) {
            case "20" -> "NSW";
            case "30" -> "VIC";
            case "40" -> "QLD";
            default -> switch (owner.getCity() == null ? "" : owner.getCity()) {
                case "Sydney" -> "NSW";
                case "Melbourne" -> "VIC";
                case "Brisbane" -> "QLD";
                default -> "UNKNOWN";
            };
        };
    }

    /** The first 8 upper-case hex characters of SHA-256 over {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
