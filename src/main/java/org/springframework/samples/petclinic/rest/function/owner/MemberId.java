package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.function.common.Luhn;

/**
 * The single definition of an owner's {@code memberId} format — {@code <REGION><FY><HASH8><CHK>},
 * the identity version-2 region code, the 2-digit fiscal year, the 8-character hash and a single Luhn
 * check digit, concatenated with no separator (e.g. {@code NSWV2261A2B3C4D7}, where {@code NSWV2} is the
 * plain {@code NSW} region with the {@code V2} version tag mixed in by {@link AssignOwnerMemberId}).
 *
 * <p>{@code CHK} is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}
 * (the region and hash letters are ignored by {@link Luhn#checkDigit}). It unifies what used to be the
 * separate {@code customerCode}, {@code membershipNumber} and {@code checkDigit}.
 *
 * <p>The user-facing locality is <em>not</em> read back out of the member id — it is derived from the
 * postcode as the plain region — so the {@code V2} tag embedded here never leaks into that field.
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

    private MemberId() {
    }
}
