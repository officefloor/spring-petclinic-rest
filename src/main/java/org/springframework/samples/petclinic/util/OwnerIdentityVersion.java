package org.springframework.samples.petclinic.util;

/**
 * Version 2 of the owner identity. A single source of truth for both the numeric version — surfaced
 * as the owner response's {@code apiVersion} and the audit event's {@code schemaVersion} — and the
 * fixed {@code 'V2'} version tag that is mixed into every derived identifier (the {@link MemberId
 * member id}'s region, the household id and the identity key) so a version-2 value can never repeat a
 * version-1 one.
 *
 * <p>The tag is confined to the identifiers: it never appears in the user-facing locality, timezone
 * or {@link OwnerSegment owner segment}, which continue to use the plain {@link OwnerRegion region}.
 */
public final class OwnerIdentityVersion {

    /** The identity version, reported as {@code apiVersion} and audit {@code schemaVersion}. */
    public static final int VERSION = 2;

    /** The fixed tag mixed into every derived identifier so v2 values never collide with v1 ones. */
    public static final String TAG = "V2";

    private OwnerIdentityVersion() {
    }
}
