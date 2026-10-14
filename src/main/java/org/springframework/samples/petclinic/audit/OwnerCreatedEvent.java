package org.springframework.samples.petclinic.audit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event published to the {@code AUDIT} logger when an owner is created.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the id of the created owner
 * @param customerCode    the owner's current primary identifier
 * @param membershipLevel the owner's membership level at creation
 * @param event           always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
                                String event) {

    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Builds the event for a saved owner. The owner's primary identifier is read here and only here,
     * so a change of identifier (e.g. to a member id) is made in this one place.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getCustomerCode(), membershipLevel, OWNER_CREATED);
    }
}
