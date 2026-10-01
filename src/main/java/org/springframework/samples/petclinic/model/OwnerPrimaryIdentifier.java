package org.springframework.samples.petclinic.model;

/**
 * Single source of truth for an owner's <em>primary identifier</em>: the one human-facing value that
 * uniquely names the owner in external records and events. That is the {@link Owner#getMemberId()
 * memberId} - the unified identifier that replaced the former customer code and membership number - and
 * every consumer (such as the {@code OWNER_CREATED} audit event) follows from this method.
 *
 * <p>This is a model-level derivation of an {@link Owner} (like {@link MemberId} and
 * {@link OwnerIdentity}): it exists so callers never hard-code which field is the primary identifier.
 */
public final class OwnerPrimaryIdentifier {

    private OwnerPrimaryIdentifier() {
    }

    /** The current primary identifier of a persisted or in-flight owner. */
    public static String of(Owner owner) {
        return owner.getMemberId();
    }
}
