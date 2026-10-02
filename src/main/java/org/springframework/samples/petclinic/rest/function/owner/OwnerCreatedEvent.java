package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created.
 *
 * <p>Serialised to JSON as {@code {seq, ownerId, customerCode, membershipLevel, event}} — the record
 * component order is the JSON field order. {@code seq} is a monotonically increasing integer across
 * creates.
 *
 * <p>The {@code customerCode} component carries the owner's <em>current primary identifier</em>. It is
 * sourced from {@link #from(int, Owner)} via {@link Owner#getCustomerCode()}, so when the customerCode is
 * later unified into the memberId — i.e. once {@code getCustomerCode()} yields the memberId — this event
 * carries the memberId without any change here.
 */
public record OwnerCreatedEvent(int seq, int ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /**
     * Builds the event for a freshly created owner, stamping it with the given sequence number and the
     * owner's current primary identifier.
     */
    public static OwnerCreatedEvent from(int seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getCustomerCode(),
                owner.getMembershipLevel(), EVENT_TYPE);
    }
}
