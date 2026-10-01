package org.springframework.samples.petclinic.model;

/**
 * Single source of truth for an owner's <em>primary identifier</em>: the one human-facing value that
 * uniquely names the owner in external records and events. Today that is the {@link Owner#getCustomerCode()
 * customerCode}; when the customer code is later unified into the member id, this method changes to return
 * the member id and every consumer (such as the {@code OWNER_CREATED} audit event) follows automatically.
 *
 * <p>This is a model-level derivation of an {@link Owner} (like {@link MembershipNumber} and
 * {@link OwnerIdentity}): it exists so callers never hard-code which field is the primary identifier.
 */
public final class OwnerPrimaryIdentifier {

    private OwnerPrimaryIdentifier() {
    }

    /** The current primary identifier of a persisted or in-flight owner. */
    public static String of(Owner owner) {
        return owner.getCustomerCode();
    }
}
