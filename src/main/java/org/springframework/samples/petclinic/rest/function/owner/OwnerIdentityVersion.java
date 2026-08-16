package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Version 2 of the owner identity derivation.
 *
 * <p>The fixed {@link #TAG "V2"} version tag is mixed into the region code embedded in the
 * {@code memberId}, the {@code householdId} and the {@code identityKey}, so every version-2
 * identifier differs from its version-1 form and no version-1 value is ever produced again. The tag
 * appears only inside those identifiers — never in the user-facing {@code locality}, {@code timezone}
 * or the owner segment's derived region, which continue to read the plain region code.
 *
 * <p>{@link #API_VERSION} is the top-level {@code apiVersion} of the owner response and
 * {@link #SCHEMA_VERSION} is the {@code schemaVersion} carried on the structured audit event.
 */
public final class OwnerIdentityVersion {

    private OwnerIdentityVersion() {
    }

    /** The fixed identity version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    /** The owner response {@code apiVersion}. */
    public static final int API_VERSION = 2;

    /** The structured audit event {@code schemaVersion}. */
    public static final int SCHEMA_VERSION = 2;
}
