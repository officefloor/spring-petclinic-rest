package org.springframework.samples.petclinic.model;

/**
 * Assembles an owner's unified {@code memberId}, formatted
 * {@code '<REGION><FY><HASH8><CHK>'}: REGION is the version-2 region code derived from the
 * owner's postcode with the fixed {@code 'V2'} version tag mixed in (see
 * {@link Locality#identityRegionOf}); FY is the two-digit {@link FiscalYear} of the
 * owner's (business-day-adjusted) registrationDate; HASH8 is the owner's identity hash (see
 * {@link Hash8}); and CHK is a single {@link Luhn} check digit computed over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code 'NSW261A2B3C4D7'}).
 *
 * <p>This unifies the former {@code customerCode}, {@code membershipNumber} and
 * {@code checkDigit}: the region-and-hash identity, the fiscal year and the Luhn digit are
 * now a single value. This is the one place the id is composed from its segments, so the
 * assignment step, the owner response, the locality and the audit trail all read the same
 * value rather than each rebuilding it. Null when the owner has no registrationDate.
 */
public final class MemberId {

    private MemberId() {
    }

    /** The base memberId for the given owner (before collision handling), or null when it cannot be derived. */
    public static String of(Owner owner) {
        if (owner.getRegistrationDate() == null) {
            return null;
        }
        String region = Locality.identityRegionOf(owner.getPostcode());
        String fy = FiscalYear.shortYear(owner.getRegistrationDate());
        String hash8 = Hash8.of(owner);
        String withoutCheck = region + fy + hash8;
        return withoutCheck + Luhn.digit(withoutCheck);
    }
}
