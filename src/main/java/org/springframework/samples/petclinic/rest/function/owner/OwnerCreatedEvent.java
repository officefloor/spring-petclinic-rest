package org.springframework.samples.petclinic.rest.function.owner;

import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * An immutable structured audit event for a created owner. Serialized to the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event}} (field order preserved),
 * where {@code event} is always {@link #EVENT_NAME} and {@code customerCode} carries the
 * owner's {@link Owner#getPrimaryIdentifier() current primary identifier} — so when that
 * identifier becomes the member id, this event carries the member id without further change.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        int membershipLevel, String event) {

    /** The fixed event marker for a created owner. */
    public static final String EVENT_NAME = "OWNER_CREATED";

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * The event for {@code owner}, stamped with {@code seq}, reading the owner's primary
     * identifier and current membership level.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getPrimaryIdentifier(),
                MembershipLevel.of(owner), EVENT_NAME);
    }

    /** This event as its JSON representation. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
