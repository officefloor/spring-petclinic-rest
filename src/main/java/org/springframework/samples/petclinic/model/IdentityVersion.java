package org.springframework.samples.petclinic.model;

/**
 * The single definition of the owner-identity version. Everything that derives an owner
 * identifier — the member id (see {@link MemberId}), the identity key (see {@link OwnerIdentity})
 * and the household id — mixes {@link #TAG} into its input, so every identifier produced under
 * version 2 differs from the version-1 value and no version-1 value is ever produced again.
 *
 * <p>The tag lives <em>inside</em> the identifiers only. User-facing derived fields that are not
 * identifiers — the plain region {@code locality}, the {@code timezone} and the response's owner
 * segment — never carry it.
 */
public final class IdentityVersion {

    /** The fixed version tag mixed into every version-2 owner identifier. */
    public static final String TAG = "V2";

    /** The numeric owner-identity version, surfaced as the response {@code apiVersion} and the
     * audit event {@code schemaVersion}. */
    public static final int NUMBER = 2;

    private IdentityVersion() {
    }
}
