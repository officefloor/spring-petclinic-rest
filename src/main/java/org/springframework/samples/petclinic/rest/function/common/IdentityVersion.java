package org.springframework.samples.petclinic.rest.function.common;

/**
 * The single definition of the owner identity version. Version 2 mixes a fixed {@link #TAG} into every
 * derived identifier — the {@code householdId}, the {@code identityKey} and the region code embedded in
 * the {@code memberId} — so every identifier differs from the value version 1 produced and no version-1
 * value is ever produced again.
 *
 * <p>The tag is confined to the identifiers themselves: the user-facing {@code locality}, {@code
 * timezone} and the owner segment's derived region stay the plain region code (e.g. {@code NSW}). The
 * same {@link #VERSION} number surfaces as the owner response's {@code apiVersion} and the audit event's
 * {@code schemaVersion}, so all three move together and cannot drift apart.
 */
public final class IdentityVersion {

    /** The fixed version tag mixed into every derived identifier under identity version 2. */
    public static final String TAG = "V2";

    /** The identity version number surfaced as {@code apiVersion} and the audit {@code schemaVersion}. */
    public static final int VERSION = 2;

    private IdentityVersion() {
    }
}
