package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Locality;

/**
 * Version 2 of the owner identity — the single source of how the {@code V2} release is mixed into
 * every derived identifier.
 *
 * <p>A fixed {@link #TAG} is mixed into each identifier: into the region code that prefixes the
 * memberId (see {@link #identifierRegion}) and into the hash pre-image of the householdId and the
 * identityKey (see {@link #tagged}). Because the tag participates in every derivation, no value
 * produced under version 1 can ever be produced again.
 *
 * <p>The tag appears <b>only</b> inside the identifiers. The user-facing {@code locality} and
 * {@code timezone} fields and the owner segment's derived area keep the plain region (see
 * {@link Locality#forPostcode}).
 *
 * <p>{@link #VERSION} is the matching contract number, surfaced as the owner response's
 * {@code apiVersion} and the owner-created audit event's {@code schemaVersion}.
 */
public final class OwnerIdentityVersion {

    /** The owner-identity contract version. */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into every derived identifier (e.g. {@code "V2"}). */
    static final String TAG = "V" + VERSION;

    private OwnerIdentityVersion() {
    }

    /**
     * The region code used inside the identifiers: the plain region derived from the postcode (see
     * {@link Locality#forPostcode}) with the version {@link #TAG} mixed in. This is deliberately
     * distinct from the user-facing locality, which stays the plain region.
     */
    static String identifierRegion(String postcode) {
        return Locality.forPostcode(postcode) + TAG;
    }

    /**
     * Mixes the version {@link #TAG} into a hash pre-image, so a version-2 hash never reproduces the
     * version-1 value derived from the same components.
     */
    static String tagged(String preImage) {
        return TAG + "|" + preImage;
    }
}
