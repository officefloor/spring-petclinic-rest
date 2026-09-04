package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.LocalityLookup;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The REGION segment of an owner's region-and-hash identity (see {@link AssignMemberId}). Two forms:
 * <ul>
 * <li>{@link #of(Owner)} — the <em>plain</em> region derived from the owner's postcode with the city
 * as a fallback (see {@link LocalityLookup#regionOf(String, String)}), for example {@code NSW}. This
 * is the user-facing 'locality' (and drives the timezone and owner segment); it never carries a
 * version tag.</li>
 * <li>{@link #identityRegion(Owner)} — the <em>version-2</em> region code embedded inside the derived
 * identifiers ({@code memberId}, {@code householdId}, {@code identityKey}). It is the plain region
 * with a fixed {@code V2} version tag mixed in (for example {@code V2NSW}), so every version-2
 * identifier differs from its version-1 form and no version-1 value is produced again.</li>
 * </ul>
 * Because the {@code V2} tag lives only in {@link #identityRegion}, it appears solely inside the
 * identifiers, never in the plain region surfaced as 'locality', 'timezone' or the owner segment.
 */
public final class OwnerRegion {

    /** The fixed version-2 tag mixed into the region code embedded in the version-2 identifiers. */
    static final String IDENTITY_VERSION_TAG = "V2";

    private OwnerRegion() {
    }

    /** The plain REGION segment for {@code owner} (the user-facing locality, e.g. {@code NSW}). */
    public static String of(Owner owner) {
        return LocalityLookup.regionOf(owner.getCity(), owner.getPostcode());
    }

    /** The plain REGION a create request resolves to, from its city and postcode. */
    static String of(OwnerFieldsDto request) {
        return LocalityLookup.regionOf(request.getCity(), request.getPostcode());
    }

    /**
     * The version-2 region code embedded inside the identifiers: the plain region with the fixed
     * {@code V2} version tag mixed in (e.g. {@code V2NSW}).
     */
    public static String identityRegion(Owner owner) {
        return IDENTITY_VERSION_TAG + of(owner);
    }

    /** The version-2 region code a create request would embed in its identifiers. */
    static String identityRegion(OwnerFieldsDto request) {
        return IDENTITY_VERSION_TAG + of(request);
    }

    /**
     * The plain region recovered from the version-2 identity region (the {@code V2} tag stripped) —
     * how the owner segment derives its region from the version-2 identity without ever carrying the
     * version tag.
     */
    public static String segmentRegion(Owner owner) {
        return identityRegion(owner).substring(IDENTITY_VERSION_TAG.length());
    }
}
