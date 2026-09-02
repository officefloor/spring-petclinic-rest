package org.springframework.samples.petclinic.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a single {@code memberId} formatted
 * {@code <REGION><FY><HASH8><CHK>}, where REGION is the region code derived from the owner's
 * postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, else {@code UNKNOWN}), FY is the two-digit
 * registration-date fiscal year, HASH8 is the first eight upper-case hex characters of SHA-256 over
 * the normalized telephone concatenated with the last name (the same HASH8 used by the
 * region-and-hash identity), and CHK is a single Luhn check digit computed over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code NSW271A2B3C4D8}). Kept as a small, self-contained unit so
 * the rule can be applied from the create flow without adding complexity to the controller or service.
 */
public final class OwnerMemberIdPolicy {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerMemberIdPolicy() {
    }

    /**
     * Assign the {@code memberId} for a newly created owner.
     *
     * @param clinicService source of the existing owners, for collision handling
     * @param owner         the owner being created
     */
    public static void assignMemberId(ClinicService clinicService, Owner owner) {
        String body = region(owner.getPostcode()) + fiscalYear(owner) + hash8(owner);
        owner.setMemberId(deduplicate(clinicService, body + luhn(body)));
    }

    /** {@code base}, or {@code base-<n>} with the smallest {@code n >= 2} not already in use. */
    private static String deduplicate(ClinicService clinicService, String base) {
        Set<String> used = new HashSet<>();
        for (Owner existing : clinicService.findAllOwners()) {
            used.add(existing.getMemberId());
        }
        String candidate = base;
        for (int n = 2; used.contains(candidate); n++) {
            candidate = base + "-" + n;
        }
        return candidate;
    }

    /** The region whose postcode range contains {@code postcode}, or {@code UNKNOWN}. */
    static String region(String postcode) {
        if (postcode != null) {
            int code = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
                int[] range = entry.getValue();
                if (code >= range[0] && code <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return "UNKNOWN";
    }

    /** Two-digit registration-date fiscal year segment. */
    private static String fiscalYear(Owner owner) {
        return String.format("%02d", OwnerFiscalYearPolicy.fiscalYearOf(owner.getRegistrationDate()) % 100);
    }

    /** First eight upper-case hex characters of SHA-256(normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        String input = orEmpty(owner.getTelephone()) + orEmpty(owner.getLastName());
        return Sha256Hex.upper(input, 4);
    }

    /** Luhn check digit (0-9) over the digits contained in {@code s}. */
    private static int luhn(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    private static String orEmpty(String value) {
        return OwnerFieldNormalizer.orEmpty(value);
    }
}
