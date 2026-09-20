package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Version 2 of the owner identity. Every derived identifier — the {@link MemberId member id},
 * the {@link Households household id} and the {@link IdentityKeys identity key} — mixes this
 * version's {@link #TAG tag} into the value it hashes, and the region code embedded inside the
 * member id is tagged the same way. Mixing the tag in shifts every hash, so no version-2
 * identifier can coincide with the version-1 value it replaces.
 *
 * <p>The tag stays confined to the identifiers. The user-facing {@code locality} and
 * {@code timezone} fields and the owner segment's derived region remain the plain region code
 * (e.g. {@code NSW}); only the numeric {@link #NUMBER version number} surfaces elsewhere, as the
 * response {@code apiVersion} and the audit event {@code schemaVersion}.
 */
public final class IdentityVersion {

    /** The identity version number, surfaced as the response apiVersion and audit schemaVersion. */
    public static final int NUMBER = 2;

    /** The tag mixed into every derived identifier so version-2 values never collide with version-1. */
    static final String TAG = "V2";

    private IdentityVersion() {
    }

    /** {@code input} with the version tag mixed in, ready to be hashed into an identifier. */
    static String stamp(String input) {
        return TAG + "|" + input;
    }

    /** The region code as embedded inside an identifier: the plain region tagged with the version. */
    static String region(String plainRegion) {
        return plainRegion + TAG;
    }
}
