package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner's primary identifier: the stable code that identifies
 * the owner across the system and in its audit trail. This is the unified
 * {@link Owner#getMemberId() memberId}; resolving it here in one place keeps every consumer
 * (the {@link OwnerCreatedEvent} included) carrying the same identifier.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's current primary identifier. */
    public static String primary(Owner owner) {
        return owner.getMemberId();
    }
}
