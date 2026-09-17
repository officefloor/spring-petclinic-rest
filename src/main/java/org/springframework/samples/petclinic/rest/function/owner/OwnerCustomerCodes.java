package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Shared formatting for an owner's customer code: {@code <REGION>-<HASH8>}, where REGION is the
 * region derived from the postcode (see {@link Locality#forPostcode(String)}) and HASH8 the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone followed by the last
 * name (e.g. {@code NSW-1A2B3C4D}). The code is a stable function of the owner's own identity,
 * carrying no sequence number.
 */
final class OwnerCustomerCodes {

    /** Number of leading SHA-256 hex characters that form the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    private OwnerCustomerCodes() {
    }

    /** The {@code <REGION>-<HASH8>} customer code for {@code owner}. */
    static String forOwner(Owner owner) {
        return format(owner.getPostcode(), owner.getTelephone(), owner.getLastName());
    }

    /** Format {@code postcode}, {@code telephone} and {@code lastName} as {@code <REGION>-<HASH8>}. */
    static String format(String postcode, String telephone, String lastName) {
        return Locality.forPostcode(postcode) + "-" + hash8(telephone, lastName);
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
