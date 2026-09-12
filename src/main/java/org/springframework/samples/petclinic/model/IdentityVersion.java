package org.springframework.samples.petclinic.model;

/**
 * Version 2 of the owner identity. Everything that distinguishes an owner's
 * <em>identifiers</em> from version 1 is anchored here in one place.
 *
 * <p>{@link #TAG} is the fixed {@code 'V2'} version tag mixed into every identifier so it is
 * rederived and no value produced under version 1 is produced again: it seeds the region code
 * used inside the {@code memberId} (see {@link Locality#identityRegionOf(String)}), and the
 * hashes behind the {@code householdId} and the {@code identityKey}. The tag lives only inside
 * the identifiers — it never appears in the user-facing {@code locality} (which stays the plain
 * region code, e.g. {@code 'NSW'}), the {@code timezone} or the owner segment's derived region.
 *
 * <p>{@link #API_VERSION} is the top-level {@code apiVersion} on the owner response and
 * {@link #AUDIT_SCHEMA_VERSION} is the {@code schemaVersion} on the structured audit event.
 */
public final class IdentityVersion {

    private IdentityVersion() {
    }

    /** The fixed version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    /** The {@code apiVersion} carried at the top level of the owner response. */
    public static final int API_VERSION = 2;

    /** The {@code schemaVersion} carried by the structured owner-created audit event. */
    public static final int AUDIT_SCHEMA_VERSION = 2;
}
