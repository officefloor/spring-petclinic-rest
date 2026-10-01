package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's <em>primary identifier</em> &mdash; the one value that
 * uniquely names the owner to the outside world.
 *
 * <p>Today that is the {@code customerCode}. When the {@code customerCode} is later unified into
 * the {@code memberId}, only this method changes to return the {@code memberId}; every consumer
 * (such as the {@code OWNER_CREATED} audit event emitted by {@link AuditOwnerCreated}) then carries
 * the new identifier without being touched.
 */
public final class OwnerPrimaryIdentifier {

    private OwnerPrimaryIdentifier() {
    }

    /** The owner's current primary identifier. */
    public static String of(Owner owner) {
        return owner.getCustomerCode();
    }
}
