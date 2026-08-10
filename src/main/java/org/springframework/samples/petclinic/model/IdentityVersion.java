package org.springframework.samples.petclinic.model;

/**
 * Version 2 of the owner identity scheme. Every identifier — the region code used inside the
 * {@code memberId}, the {@code memberId} itself, the {@code householdId} and the {@code identityKey}
 * — mixes in the fixed {@link #TAG "V2"} version tag, so a value produced under version 1 is never
 * produced again.
 *
 * <p>The tag is an <em>internal</em> identifier detail only: the user-facing region ('locality'),
 * 'timezone' and the owner segment's derived region stay the plain region code (e.g. {@code "NSW"}),
 * recovered with {@link #stripTag(String)}.
 */
public final class IdentityVersion {

    /** The fixed identity-scheme version tag mixed into every version-2 identifier. */
    public static final String TAG = "V2";

    /** The numeric API version reported at the top level of the owner response. */
    public static final int API_VERSION = 2;

    private IdentityVersion() {
    }

    /**
     * The given identifier with a single leading {@link #TAG} removed when present, so a plain
     * region code can be recovered from a version-2 {@code memberId}. Returns the value unchanged
     * (including {@code null}) when it carries no leading tag.
     */
    public static String stripTag(String value) {
        if (value != null && value.startsWith(TAG)) {
            return value.substring(TAG.length());
        }
        return value;
    }
}
