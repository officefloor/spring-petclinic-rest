package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialised to JSON on the
 * {@code AUDIT} logger by {@link EmitOwnerCreatedEvent}.
 *
 * <p>The identifier field carries the owner's current
 * {@link Owner#getPrimaryIdentifier() primary identifier} &mdash; the {@code customerCode} today.
 * Sourcing it from that one accessor means that when the primary identifier is later unified into a
 * member id, the event carries the member id without any change here.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The event-type marker carried by every owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Build the event for a freshly created, persisted {@code owner}, stamped with {@code seq}. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getPrimaryIdentifier(),
                owner.getMembershipLevel(), OWNER_CREATED);
    }
}
