package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Luhn;

/**
 * Formats an owner's member id — the single identifier that unifies what were once the separate
 * customer code and membership number. It is formatted as {@code <REGION><FY><HASH8><CHK>}
 * (e.g. {@code NSWV226A2B3C4D5}):
 * <ul>
 * <li>{@code REGION} — the plain region code derived from the owner's postcode (see
 * {@link org.springframework.samples.petclinic.model.CityRegion}), tagged with the
 * {@link IdentityVersion identity version} (e.g. {@code NSW} becomes {@code NSWV2});</li>
 * <li>{@code FY} — the two-digit {@link FiscalYear fiscal year} of the registration date;</li>
 * <li>{@code HASH8} — the first 8 upper-case hex characters of
 * {@code SHA-256} over the {@link IdentityVersion#stamp versioned}
 * {@code normalizedTelephone + lastName};</li>
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
     * The member id for an owner in the plain {@code region} registered on
     * {@code registrationDate} whose normalized telephone and last name are given. The region is
     * tagged and the hash versioned via {@link IdentityVersion} so the id belongs to version 2.
     */
    public static String format(String region, LocalDate registrationDate,
            String normalizedTelephone, String lastName) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        String fy = String.format("%02d", FiscalYear.of(registrationDate) % 100);
        String hash8 = Hashes.upperHexPrefix(IdentityVersion.stamp(telephone + last), HASH_LENGTH);
        String base = IdentityVersion.region(region) + fy + hash8;
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
}
