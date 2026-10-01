package org.springframework.samples.petclinic.model;

/**
 * The single source of truth for the owner identity scheme version. Version 2 rederives every owner
 * identifier — the region code baked into the {@code memberId}, the {@code householdId} and the
 * {@code identityKey} — by mixing in a fixed {@link #TAG version tag}, so that no identifier produced
 * under version 1 is ever produced again.
 *
 * <p>The tag is mixed only <em>inside</em> the identifiers. The user-facing {@code locality} (and the
 * {@code timezone} and owner-segment region derived from it) stay the plain region code (e.g.
 * {@code NSW}); the tag never appears there. The same {@link #VERSION} number is the top-level
 * {@code apiVersion} of the owner response and the {@code schemaVersion} of the owner-created audit
 * event, so the response shape, the audit schema and the identity derivation move in lock-step.
 */
public final class IdentityVersion {

    /** The current owner-identity scheme version: the {@code apiVersion} and audit {@code schemaVersion}. */
    public static final int VERSION = 2;

    /** The fixed version tag mixed into every version-2 identifier (and nowhere else). */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
