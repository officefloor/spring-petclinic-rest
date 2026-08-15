package org.springframework.samples.petclinic.rest.function.common;

/**
 * Version 2 of the owner identity. Every derived identifier — the {@code memberId}, the
 * {@code identityKey} and the {@code householdId} — mixes in the fixed {@link #TAG version tag} so
 * that a version-2 value is never one that version 1 could have produced. The tag is confined to the
 * identifiers: the user-facing {@code locality}, {@code timezone} and the owner segment's derived
 * region stay the plain region code (for example {@code "NSW"}).
 *
 * <p>{@link #VERSION} is the integer surfaced to clients as the owner response's {@code apiVersion}
 * and carried on the structured audit event as its {@code schemaVersion}.
 */
public final class IdentityVersion {

    /** The current identity version — the response {@code apiVersion} and audit {@code schemaVersion}. */
    public static final int VERSION = 2;

    /** The fixed tag mixed into every derived identifier so v2 values are disjoint from v1. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
