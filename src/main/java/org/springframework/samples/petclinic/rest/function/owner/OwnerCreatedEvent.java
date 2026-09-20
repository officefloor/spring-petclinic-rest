package org.springframework.samples.petclinic.rest.function.owner;

import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;

/**
 * An immutable structured audit event for a created owner. Serialized to the JSON object
 * {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment, event}} (field
 * order preserved), where {@code schemaVersion} is the {@link IdentityVersion#NUMBER identity
 * version}, {@code event} is always {@link #EVENT_NAME}, {@code memberId} carries the owner's
 * {@link Owner#getPrimaryIdentifier() primary identifier} and {@code ownerSegment} its recomputed
 * {@link OwnerSegment#of(Owner) marketing segment}.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** The fixed event marker for a created owner. */
    public static final String EVENT_NAME = "OWNER_CREATED";

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * The event for {@code owner}, stamped with {@code seq}, reading the owner's primary
     * identifier, current membership level and recomputed owner segment.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(IdentityVersion.NUMBER, seq, owner.getId(),
                owner.getPrimaryIdentifier(), MembershipLevel.of(owner),
                OwnerSegment.of(owner), EVENT_NAME);
    }

    /** This event as its JSON representation. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
