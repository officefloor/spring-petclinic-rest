package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.function.common.Localities;
import org.springframework.samples.petclinic.rest.function.common.Luhn;

/**
 * The single definition of an owner's {@code memberId} format — {@code <REGION><FY><HASH8><CHK>},
 * the region code, the 2-digit fiscal year, the 8-character hash and a single Luhn check digit,
 * concatenated with no separator (e.g. {@code NSW261A2B3C4D7}). Co-locates construction
 * ({@link #format}) and read-back ({@link #regionOf}) so both agree on the layout and cannot drift
 * apart, the same idiom {@link IdentityKey} uses for the identity key.
 *
 * <p>{@code CHK} is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}
 * (the region and hash letters are ignored by {@link Luhn#checkDigit}). It unifies what used to be the
 * separate {@code customerCode}, {@code membershipNumber} and {@code checkDigit}.
 *
 * <p>Shared by {@link AssignOwnerMemberId} (which builds the id) and the owner mapper (which reads the
 * region back out as the owner's locality). Public because the mapper lives in another package.
 */
public final class MemberId {

    /** The character separating the base id from a de-duplication suffix. */
    private static final char SEPARATOR = '-';

    /**
     * The member id for the given region, 2-digit fiscal year and hash segment —
     * {@code <REGION><FY><HASH8><CHK>} where {@code CHK} is the Luhn check digit over the digits of
     * {@code <REGION><FY><HASH8>}.
     */
    public static String format(String region, int fiscalYearYy, String hash8) {
        String base = region + String.format("%02d", fiscalYearYy) + hash8;
        return base + Luhn.checkDigit(base);
    }

    /**
     * The de-duplicated form of {@code base} — the base member id with {@code -<n>} appended — used to
     * keep otherwise-colliding ids distinct (see {@link DeduplicateOwnerMemberId}).
     */
    public static String withSuffix(String base, int n) {
        return base + SEPARATOR + n;
    }

    /**
     * The region segment of a {@code <REGION><FY><HASH8><CHK>} member id — the leading run of letters
     * before the first digit (the fiscal year) — or {@link Localities#UNKNOWN} when the id is absent,
     * blank or does not start with a region code.
     */
    public static String regionOf(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return Localities.UNKNOWN;
        }
        int i = 0;
        while (i < memberId.length() && !Character.isDigit(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? Localities.UNKNOWN : memberId.substring(0, i);
    }

    private MemberId() {
    }
}
