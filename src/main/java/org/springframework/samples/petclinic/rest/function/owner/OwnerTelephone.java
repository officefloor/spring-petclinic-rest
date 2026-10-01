package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner-telephone normalization rule: strip every non-digit
 * character. Both {@link ValidateOwnerFields} (which normalizes the submitted value) and
 * {@link CheckOwnerTelephoneUnique} (which compares against stored owners) use this so the two can
 * never drift apart.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
