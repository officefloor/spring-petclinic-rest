package org.springframework.samples.petclinic.rest.audit;

import java.time.LocalDate;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevels;

/**
 * Immutable structured audit event for a newly created owner, rendered as a single-line JSON
 * object {@code {seq, ownerId, customerCode, membershipLevel, event}}.
 *
 * <p>The event carries the owner's current primary identifier. Today that identifier is the
 * customer code, so it is read in {@link #of} alone: when the primary identifier is later
 * unified into a member id, only that one place changes and the event carries the member id
 * instead.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The {@code event} discriminator carried by every owner-creation event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * Build the event for {@code owner} (already persisted, so its id is set) at sequence number
     * {@code seq}, deriving the effective membership level as at {@code asOf}. The owner's primary
     * identifier is read here, in one place.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, LocalDate asOf) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getCustomerCode(),
                MembershipLevels.levelOf(owner, asOf), OWNER_CREATED);
    }

    /** Render this event as a compact single-line JSON object. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
