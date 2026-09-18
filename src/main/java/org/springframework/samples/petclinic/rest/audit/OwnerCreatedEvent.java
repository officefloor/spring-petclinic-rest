package org.springframework.samples.petclinic.rest.audit;

import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event recording that an owner was created, emitted alongside the
 * human-readable audit line for {@code POST /api/owners}. Serialized to a JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event}} where {@code event} is always
 * {@code OWNER_CREATED} and {@code seq} is a monotonically increasing sequence across creates.
 *
 * <p>The event carries the owner's <em>current primary identifier</em>, the {@code memberId},
 * resolved in one place ({@link #primaryIdentifier(Owner)}).
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String event) {

    private static final String EVENT_TYPE = "OWNER_CREATED";

    /** Builds the event for a persisted {@code owner} at the given monotonic {@code seq}. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), primaryIdentifier(owner),
                owner.getMembershipLevel(), EVENT_TYPE);
    }

    /** The owner's current primary identifier, the {@code memberId}. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /** Renders this event as a JSON object string. */
    public String toJson(JsonMapper mapper) {
        return mapper.writeValueAsString(this);
    }
}
