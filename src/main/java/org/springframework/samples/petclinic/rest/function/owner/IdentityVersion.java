package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of the owner identity <em>version</em>. Version 2 rederives every
 * owner identifier — the {@link MemberId member id}, the {@link Household household id} and
 * the {@link IdentityKey identity key} — by mixing a fixed {@code "V2"} tag into each one's
 * hash preimage (see {@link #tag(String)}), so a version-2 identifier can never collide with
 * a value a version-1 owner was given.
 *
 * <p>The tag lives <em>inside</em> the identifiers only. The user-facing region fields
 * ('locality', 'timezone' and the owner segment's derived region) stay the plain region code
 * (e.g. {@code "NSW"}) — they never carry the tag.
 *
 * <p>{@link #API_VERSION} is echoed to clients as the top-level {@code apiVersion} of an owner
 * response; {@link #AUDIT_SCHEMA_VERSION} stamps the owner-created audit event.
 */
public final class IdentityVersion {

    /** The API version reported on every owner response. */
    public static final int API_VERSION = 2;

    /** The schema version stamped on the owner-created audit event. */
    public static final int AUDIT_SCHEMA_VERSION = 2;

    /** The fixed version tag mixed into every identifier's hash preimage. */
    private static final String TAG = "V2";

    private IdentityVersion() {
    }

    /** The {@code preimage} of an identifier hash prefixed with the version tag, so a
     *  version-2 identifier is derived from a namespace disjoint from version 1. */
    public static String tag(String preimage) {
        return TAG + "|" + preimage;
    }
}
