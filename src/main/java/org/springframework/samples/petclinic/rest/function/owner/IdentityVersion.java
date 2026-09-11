package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single source of the owner-identity algorithm version. Version 2 mixes a fixed
 * {@code "V2"} tag into every derived identifier — the region code used inside the
 * {@link MemberIds memberId}, the {@link Households householdId} and the
 * {@link IdentityKeys identityKey} — so that every identifier changes and no value
 * produced under version 1 is produced again.
 *
 * <p>The tag is an identifier-only concern: it appears inside the identifiers but never in
 * the user-facing {@code locality}, {@code timezone} or the {@code ownerSegment}'s derived
 * region, all of which continue to use the plain region code.
 */
public final class IdentityVersion {

    private IdentityVersion() {
    }

    /** The fixed version tag mixed into every version-2 identity derivation. */
    public static final String TAG = "V2";
}
