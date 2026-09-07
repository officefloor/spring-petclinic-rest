package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The version of the owner-identity derivation. Version 2 mixes a fixed {@code 'V2'} tag into the
 * region code used INSIDE every identifier — the {@link MemberId member id}, the
 * {@link OwnerIdentity identity key} and the {@link Household household id} — so every identifier
 * changes and no value produced under version 1 is produced again.
 *
 * <p>The tag lives INSIDE the identifiers only. The user-facing {@code locality}, its
 * {@code timezone} and the {@link OwnerSegment owner segment}'s derived region stay the plain region
 * code (e.g. {@code 'NSW'}) and never carry the tag.
 */
final class IdentityVersion {

    /** The API/identity version. Surfaced on the owner response as {@code apiVersion}. */
    static final int VERSION = 2;

    /** The fixed version tag mixed into the region code used inside every version-2 identifier. */
    static final String TAG = "V2";

    private IdentityVersion() {
    }

    /**
     * The region code used INSIDE the identifiers: the plain {@code region} with the version tag
     * mixed in (e.g. {@code 'NSW'} -> {@code 'V2NSW'}). Only the identifiers see this form; the
     * user-facing locality keeps the plain region.
     */
    static String region(String region) {
        return TAG + region;
    }
}
