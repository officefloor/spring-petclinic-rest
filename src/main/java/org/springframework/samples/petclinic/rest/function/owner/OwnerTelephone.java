package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the "normalized telephone" concept shared by the create pipeline:
 * {@link ValidateOwner} normalizes the submitted value before storing it, and
 * {@link CheckOwnerTelephoneUnique} normalizes stored owner values when comparing for duplicates.
 * Keeping the rule here ensures the value that gets stored and the value compared for uniqueness can
 * never silently diverge.
 */
public final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /** Reduce a telephone to its digits; {@code null} becomes the empty string. */
    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
