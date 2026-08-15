package org.springframework.samples.petclinic.util;

/**
 * Single source of truth for the owner identity release version. Version 2 mixes a fixed
 * {@link #TAG} into the region code carried inside the identifiers and into every identifier
 * derivation ({@code memberId}, {@code householdId}, {@code identityKey}), so every identifier
 * changes and no value produced under version 1 is produced again. The tag is confined to the
 * identifiers: the plain {@code locality}, {@code timezone} and the {@code ownerSegment} region
 * never carry it.
 *
 * <p>{@link #API_VERSION} is echoed as the owner response's top-level {@code apiVersion} and
 * {@link #AUDIT_SCHEMA_VERSION} as the owner-created audit event's {@code schemaVersion}.
 */
public final class IdentityVersion {

    /** The fixed version tag mixed into the region code and every identifier under version 2. */
    public static final String TAG = "V2";

    /** The owner identity contract version echoed as the response's {@code apiVersion}. */
    public static final int API_VERSION = 2;

    /** The audit event schema version echoed as the event's {@code schemaVersion}. */
    public static final int AUDIT_SCHEMA_VERSION = 2;

    private IdentityVersion() {
    }
}
