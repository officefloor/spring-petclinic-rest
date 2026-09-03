package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The region code used INSIDE the owner identifiers (memberId, householdId, identityKey).
 *
 * <p>Version 2 mixes a fixed {@code V2} version tag into the plain region so every identifier
 * changes and no value produced under version 1 is produced again. The tag lives only here, so it
 * never leaks into the user-facing {@code locality}, {@code timezone} or owner-segment region,
 * which keep using {@link Locality#region(Owner)} directly.
 */
public final class IdentityRegion {

    /** Fixed identity version tag. */
    static final String VERSION = "V2";

    private IdentityRegion() {
    }

    public static String code(Owner owner) {
        return VERSION + Locality.region(owner);
    }
}
