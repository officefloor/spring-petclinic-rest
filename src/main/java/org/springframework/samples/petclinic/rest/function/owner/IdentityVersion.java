package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The version of the owner identity, kept in one place so every derived identifier and the API
 * response agree on it. Version 2 mixes a fixed {@link #TAG "V2"} version tag into each identifier —
 * the {@link Household#id(String, String) householdId}, the
 * {@link OwnerIdentity#key(String, String, String) identityKey} and the region code embedded inside
 * the {@link MemberId member id} — so every identifier changes and no value produced under version 1
 * is produced again.
 *
 * <p>The tag is confined to the identifiers. The user-facing region is not an identifier: the plain
 * region code (e.g. {@code NSW}) is still what {@link Locality}, {@link Timezone} and the
 * {@link OwnerSegment owner segment} derive, so the {@code V2} tag never appears in {@code locality},
 * {@code timezone} or the owner segment's derived region. Because the member id embeds the plain
 * region as the leading part of its region segment (region then {@code V2}),
 * {@link MemberId#region(String, java.util.Set)} still reads the plain region back out.
 *
 * <p>{@link #NUMBER} is exposed on the owner response as {@code apiVersion} and stamped on the
 * structured audit event as {@code schemaVersion}.
 */
public final class IdentityVersion {

    /** The current identity/API version number, exposed as {@code apiVersion} / {@code schemaVersion}. */
    public static final int NUMBER = 2;

    /** The fixed version tag mixed into every identifier under version 2. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
