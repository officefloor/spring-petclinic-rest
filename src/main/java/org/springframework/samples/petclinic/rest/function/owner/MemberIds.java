package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.mapper.CheckDigits;
import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYears;

/**
 * Derives an owner's {@code memberId}, the {@code <REGION><FY><HASH8><CHK>} identity every
 * other owner-id-derived value is built from.
 *
 * <p>{@code REGION} is the version-2 region code ({@link #regionV2(String)}): the fixed
 * {@link IdentityVersion#TAG "V2"} tag prepended to the region derived from the owner's
 * postcode ({@link Localities#forPostcode(String)}), or {@code V2UNKNOWN} when the postcode
 * is absent or in no known range. {@code FY} is the two-digit fiscal year of the
 * (business-day-adjusted) {@code registrationDate} (see {@link FiscalYears}), or
 * {@code 00} when unset. {@code HASH8} is the first 8 upper-case hex characters of the
 * SHA-256 digest of the normalized telephone ({@link Telephones#toE164(String)})
 * concatenated with the last name. {@code CHK} is a single Luhn check digit
 * ({@link CheckDigits#luhn(String)}) computed over the digits of {@code <REGION><FY><HASH8>}.
 * There are no sequence numbers: the base identity is a pure function of the owner's
 * own fields.
 *
 * <p>Centralising the derivation here keeps {@link AssignMemberId} (which assigns the
 * memberId) and the {@code locality} mapping (which derives the region) in exact
 * agreement.
 */
public final class MemberIds {

    private MemberIds() {
    }

    /** The base {@code <REGION><FY><HASH8><CHK>} memberId for the given owner. */
    public static String forOwner(Owner owner) {
        String region = regionV2(owner.getPostcode());
        String fy = owner.getRegistrationDate() == null ? "00"
                : String.format("%02d", FiscalYears.yearSegment(owner.getRegistrationDate()));
        String base = region + fy + hash8(owner);
        return base + CheckDigits.luhn(base);
    }

    /**
     * The owner's locality: the region derived from its postcode — the same {@code REGION}
     * segment that opens its memberId.
     */
    public static String localityOf(Owner owner) {
        return region(owner.getPostcode());
    }

    /**
     * The owner's IANA timezone, derived from its {@link #localityOf(Owner) locality}
     * via the fixed region-to-timezone table, or {@code null} when the region is unknown.
     */
    public static String timezoneOf(Owner owner) {
        return Localities.timezoneForRegion(localityOf(owner));
    }

    /** The region code derived from a postcode, or {@code UNKNOWN} when unresolved. */
    public static String region(String postcode) {
        String region = Localities.forPostcode(postcode);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * The version-2 region code used <em>inside</em> the memberId: the fixed
     * {@link IdentityVersion#TAG "V2"} tag prepended to the plain {@link #region(String)
     * region}. This is the region code the identifiers are built from, so the tag appears
     * in the memberId; the plain {@link #localityOf(Owner) locality} (and the timezone and
     * owner segment derived from it) never carries it.
     */
    public static String regionV2(String postcode) {
        return IdentityVersion.TAG + region(postcode);
    }

    /** First 8 upper-case hex chars of SHA-256 over (normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        String telephone = Telephones.toE164(owner.getTelephone());
        String lastName = owner.getLastName();
        String basis = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        return Digests.sha256Hex(basis).substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
