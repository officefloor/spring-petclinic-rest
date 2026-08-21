package org.springframework.samples.petclinic.util;

/**
 * The fixed version tag mixed into every owner identifier derivation.
 *
 * <p>Version 2 rederives the region code used inside the identifiers, the {@code householdId}, the
 * {@code identityKey} and the {@code memberId} by folding this constant {@code 'V2'} tag into each
 * hash input, so every identifier changes and no value produced under version 1 recurs. The tag is
 * an <em>identifier</em> concern only: the user-facing {@code locality}, {@code timezone} and the
 * owner segment's derived region stay the plain region code (for example {@code 'NSW'}) and never
 * carry the tag.
 */
public final class IdentityVersion {

    /** The fixed version-2 tag. */
    public static final String TAG = "V2";

    private IdentityVersion() {
    }
}
