package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner's primary identifier: the stable code that identifies
 * the owner across the system and in its audit trail. Today this is the
 * {@link Owner#getCustomerCode() customerCode}; when the customerCode is later unified into
 * the memberId, changing this one method makes every consumer (the {@link OwnerCreatedEvent}
 * included) carry the memberId instead.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's current primary identifier. */
    public static String primary(Owner owner) {
        return owner.getCustomerCode();
    }
}
