package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured record of an owner's creation, emitted to the {@code AUDIT} logger as
 * JSON alongside the human-readable audit line (see {@link AuditOwnerCreated}).
 *
 * <p>The {@code memberId} field carries the owner's current primary identifier
 * (see {@link OwnerIdentity#primary(Owner)}), resolved in one place so the event always
 * follows that identifier.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The event marker for an owner creation. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Build the event for {@code owner}, stamped with the given monotonic {@code seq}. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), OwnerIdentity.primary(owner),
                owner.getMembershipLevel(), OWNER_CREATED);
    }
}
