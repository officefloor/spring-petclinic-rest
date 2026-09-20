package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Luhn;

/**
 * Formats and reads an owner's member id — the single identifier that unifies what were once
 * the separate customer code and membership number. It is formatted as
 * {@code <REGION><FY><HASH8><CHK>} (e.g. {@code NSW261A2B3C4D5}):
 * <ul>
 * <li>{@code REGION} — the region code derived from the owner's postcode (see
 * {@link org.springframework.samples.petclinic.model.CityRegion});</li>
 * <li>{@code FY} — the two-digit {@link FiscalYear fiscal year} of the registration date;</li>
 * <li>{@code HASH8} — the first 8 upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)}, the same hash used by the region-and-hash
 * identity;</li>
 * <li>{@code CHK} — a single {@link Luhn} check digit over the digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * The id is derived purely from the owner's own fields, so the same owner always yields the same
 * id without any sequence or coordination.
 */
public final class MemberId {

    /** Number of hex characters kept from the telephone-and-name hash. */
    private static final int HASH_LENGTH = 8;

    private MemberId() {
    }

    /**
     * The member id for an owner in {@code region} registered on {@code registrationDate} whose
     * normalized telephone and last name are given.
     */
    public static String format(String region, LocalDate registrationDate,
            String normalizedTelephone, String lastName) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        String fy = String.format("%02d", FiscalYear.of(registrationDate) % 100);
        String hash8 = Hashes.upperHexPrefix(telephone + last, HASH_LENGTH);
        String base = region + fy + hash8;
        return base + Luhn.checkDigit(base);
    }

    /**
     * De-duplicates {@code memberId} against the ids already {@code taken}. Returns
     * {@code memberId} unchanged when it is free; otherwise appends {@code -<n>} with the
     * smallest {@code n} of 2 or more that yields an id not in {@code taken}
     * (e.g. {@code NSW261A2B3C4D5-2}).
     */
    public static String deduplicate(String memberId, Set<String> taken) {
        if (!taken.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The {@code REGION} segment of a member id — its leading run of letters, i.e. the region
     * the id was minted in. The fiscal-year segment that follows always begins with a digit, so
     * the region is exactly this leading alphabetic prefix.
     */
    public static String region(String memberId) {
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        return memberId.substring(0, end);
    }
}
