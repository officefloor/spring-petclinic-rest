package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds and reads an owner's {@code memberId}, the unified identity
 * {@code '<REGION><FY><HASH8><CHK>'}.
 *
 * <p>REGION is the canonical region for the owner (see {@link CityRegion}, postcode-preferred with a
 * city-table fallback). FY is the two-digit fiscal year (see {@link FiscalYear}) of the owner's
 * business-day-adjusted registration date. HASH8 is the first 8 UPPER-case hex characters of the
 * SHA-256 digest over {@code normalizedTelephone + lastName} (the same region-and-hash identity used
 * elsewhere). CHK is a single Luhn check digit (see {@link CheckDigit}) computed over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code 'NSW261A2B3C4D7'}). There are no sequence numbers.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class MemberId {

    /** Length of the {@code <FY><HASH8><CHK>} suffix that always trails the REGION: 2 + 8 + 1. */
    private static final int SUFFIX_LENGTH = 11;

    private MemberId() {
    }

    /**
     * The {@code '<REGION><FY><HASH8><CHK>'} member ID for {@code region}, the two-digit {@code fiscalYear}
     * segment, {@code normalizedTelephone} and {@code lastName}. CHK is the Luhn check digit over the
     * digits of the {@code <REGION><FY><HASH8>} core.
     */
    public static String of(String region, String fiscalYear, String normalizedTelephone, String lastName) {
        String core = region + fiscalYear + hash8(normalizedTelephone, lastName);
        return core + CheckDigit.luhn(core);
    }

    /** First 8 UPPER-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}. */
    public static String hash8(String normalizedTelephone, String lastName) {
        String input = (normalizedTelephone == null ? "" : normalizedTelephone)
                + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * The owner's locality: the REGION part of its {@code memberId} ({@code '<REGION><FY><HASH8><CHK>'}).
     * When the owner has no memberId (e.g. seed data not created through the endpoint), falls back to
     * deriving the region straight from postcode/city via {@link CityRegion}.
     */
    public static String locality(Owner owner) {
        String region = region(owner.getMemberId());
        if (region != null && !region.isEmpty()) {
            return region;
        }
        return CityRegion.locality(owner.getPostcode(), owner.getCity());
    }

    /**
     * The REGION prefix of a {@code '<REGION><FY><HASH8><CHK>'} member ID, ignoring any {@code '-<n>'}
     * collision suffix, or {@code null} when {@code memberId} is null or too short to carry one.
     */
    static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        String core = memberId.replaceFirst("-[0-9]+$", "");
        return core.length() > SUFFIX_LENGTH ? core.substring(0, core.length() - SUFFIX_LENGTH) : null;
    }
}
