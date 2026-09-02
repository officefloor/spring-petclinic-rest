package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: on creation an owner is assigned a {@code customerCode} formatted
 * {@code <REGION>-<HASH8>}, where REGION is the region code derived from the owner's postcode
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, else {@code UNKNOWN}) and HASH8 is the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone concatenated with
 * the last name (e.g. {@code NSW-1A2B3C4D}). Kept as a small, self-contained unit so the rule
 * can be applied from the create flow without adding complexity to the controller or service.
 */
public final class OwnerCustomerCodePolicy {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerCustomerCodePolicy() {
    }

    /**
     * Assign the {@code customerCode} for a newly created owner.
     *
     * @param clinicService retained for call-site compatibility; not used by the identity rule
     * @param owner         the owner being created
     */
    public static void assignCustomerCode(ClinicService clinicService, Owner owner) {
        String base = region(owner.getPostcode()) + "-" + hash8(owner);
        owner.setCustomerCode(deduplicate(clinicService, base));
    }

    /** {@code base}, or {@code base-<n>} with the smallest {@code n >= 2} not already in use. */
    private static String deduplicate(ClinicService clinicService, String base) {
        Set<String> used = new HashSet<>();
        for (Owner existing : clinicService.findAllOwners()) {
            used.add(existing.getCustomerCode());
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

    /** First eight upper-case hex characters of SHA-256(normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        String input = orEmpty(owner.getTelephone()) + orEmpty(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
