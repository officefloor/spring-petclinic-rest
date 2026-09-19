package org.springframework.samples.petclinic.rest.audit;

import java.time.LocalDate;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.MembershipLevels;
import org.springframework.samples.petclinic.util.OwnerSegment;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Immutable structured audit event for a newly created owner, rendered as a single-line JSON
 * object {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment, event}}.
 *
 * <p>This is schema version 2: it carries the {@code schemaVersion} and the owner's market
 * segment recomputed from the version-2 identity, alongside the primary member-id identifier.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, int ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** The {@code event} discriminator carried by every owner-creation event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * Build the event for {@code owner} (already persisted, so its id is set) at sequence number
     * {@code seq}, deriving the effective membership level and owner segment as at {@code asOf}.
     * The segment is recomputed from the owner's plain region (locality), not the tagged region
     * embedded in the identifiers.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, LocalDate asOf) {
        int membershipLevel = MembershipLevels.levelOf(owner, asOf);
        String locality = Postcodes.regionCode(owner.getPostcode());
        String ownerSegment = OwnerSegment.of(membershipLevel, locality).name();
        return new OwnerCreatedEvent(IdentityVersion.VERSION, seq, owner.getId(),
                owner.getMemberId(), membershipLevel, ownerSegment, OWNER_CREATED);
    }

    /** Render this event as a compact single-line JSON object. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
