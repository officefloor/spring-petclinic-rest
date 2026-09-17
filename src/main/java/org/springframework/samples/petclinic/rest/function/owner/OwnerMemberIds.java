package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Luhn;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Shared formatting for an owner's memberId: {@code <REGION><FY><HASH8><CHK>}, unifying what used to
 * be the separate customer code and membership number. REGION is the version-2 region code used
 * inside the identifiers (see {@link OwnerIdentityVersion#identifierRegion}) — the postcode region
 * with the fixed {@code V2} tag mixed in — FY the two-digit {@link FiscalYear fiscal year} of the
 * registration date, HASH8 the first eight upper-case hex characters of SHA-256 over the normalized
 * telephone followed by the last name, and CHK a single Luhn check digit (see {@link Luhn}) over the
 * digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSWV2261A2B3C4D7}). The id is a stable function
 * of the owner's own identity, carrying no sequence number.
 */
final class OwnerMemberIds {

    /** Number of leading SHA-256 hex characters that form the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    private OwnerMemberIds() {
    }

    /** The {@code <REGION><FY><HASH8><CHK>} memberId for {@code owner}. */
    static String forOwner(Owner owner) {
        String body = OwnerIdentityVersion.identifierRegion(owner.getPostcode())
                + FiscalYear.twoDigitYear(owner.getRegistrationDate())
                + hash8(owner.getTelephone(), owner.getLastName());
        return body + Luhn.checkDigit(body);
    }

    /**
     * De-duplicates {@code base} against the already-{@code taken} memberIds: returns {@code base}
     * when it is free, otherwise {@code base-<n>} using the smallest {@code n} of 2 or more that is
     * unused.
     */
    static String deduplicate(String base, Set<String> taken) {
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** First eight upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}. */
    private static String hash8(String telephone, String lastName) {
        String input = part(OwnerTelephones.toE164(telephone)) + part(lastName);
        return Sha256.hex(input).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }

    private static String part(String value) {
        return value == null ? "" : value;
    }
}
