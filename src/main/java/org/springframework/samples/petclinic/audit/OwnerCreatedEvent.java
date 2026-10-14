package org.springframework.samples.petclinic.audit;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.OwnerIdentityVersion;

/**
 * Immutable structured audit event published to the {@code AUDIT} logger when an owner is created.
 *
 * @param schemaVersion   the event schema version, always {@link #SCHEMA_VERSION}
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the id of the created owner
 * @param memberId        the owner's current primary identifier (the version-2 member id)
 * @param membershipLevel the owner's membership level at creation
 * @param event           always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
                                Integer membershipLevel, String event) {

    /** The event schema version, matching the owner identity version. */
    public static final int SCHEMA_VERSION = OwnerIdentityVersion.VERSION;

    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Builds the event for a saved owner. The owner's primary identifier is read here and only here,
     * so a change of identifier is made in this one place.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, Integer membershipLevel) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, seq, owner.getId(), owner.getMemberId(), membershipLevel, OWNER_CREATED);
    }
}
