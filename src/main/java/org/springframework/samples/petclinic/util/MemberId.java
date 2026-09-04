package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}: the region
 * (see {@link Locality}), the 2-digit fiscal year of the registration date (see {@link FiscalYear}),
 * the first 8 upper-case hex of SHA-256 over {@code normalizedTelephone + lastName} — the same HASH8
 * as the region-and-hash identity (see {@link CustomerCode}) — and a single Luhn check digit over the
 * digits of {@code <REGION><FY><HASH8>} (see {@link CheckDigit}). De-duplicated against existing
 * owners: on collision, {@code -<n>} with the smallest {@code n} of 2 or more is appended.
 */
public final class MemberId {

    private MemberId() {
    }

    /** The owner's unique {@code memberId}, de-duplicated against {@code existing}. */
    public static String assign(Owner owner, Collection<Owner> existing) {
        String base = build(owner);
        String id = base;
        int n = 1;
        while (isTaken(id, existing)) {
            id = base + "-" + (++n);
        }
        return id;
    }

    /** The {@code <REGION><FY><HASH8><CHK>} member id, before de-duplication. */
    private static String build(Owner owner) {
        String regionHash = CustomerCode.assign(owner);
        String core = CustomerCode.region(regionHash) + fiscalYear(owner)
            + regionHash.substring(regionHash.indexOf('-') + 1);
        return core + CheckDigit.luhn(core);
    }

    /** The 2-digit fiscal year of the owner's registration date. */
    private static String fiscalYear(Owner owner) {
        return String.format("%02d", Math.floorMod(FiscalYear.of(owner.getRegistrationDate()), 100));
    }

    private static boolean isTaken(String id, Collection<Owner> existing) {
        for (Owner other : existing) {
            if (id.equals(other.getMemberId())) {
                return true;
            }
        }
        return false;
    }
}
