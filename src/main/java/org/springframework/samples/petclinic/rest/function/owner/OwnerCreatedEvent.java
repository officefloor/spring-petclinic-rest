package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event emitted once an owner has been created, alongside the
 * human-readable audit line. Under schema version 2 it is serialized to a JSON object
 * {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment, event}} on the
 * dedicated {@code AUDIT} logger (see {@link AuditOwnerCreated}).
 *
 * <p>{@code seq} is a monotonically increasing integer allocated across every create for the life
 * of the application (see {@link #SEQUENCE}). The event carries the owner's <em>current primary
 * identifier</em> — the version-2 {@code memberId}. That choice lives in one place,
 * {@link #primaryIdentifier(Owner)}. {@code ownerSegment} is the owner's {@code <TIER>_<AREA>}
 * segment, recomputed from the version-2 identity (see {@link OwnerSegments}).
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** The single kind of event this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The audit schema version this event is emitted under: version 2 adds {@code ownerSegment}. */
    public static final int SCHEMA_VERSION = 2;

    /** Application-wide source of the monotonically increasing {@code seq}, shared across creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * The next event for {@code owner}, allocating the next {@code seq} from the shared
     * application-wide sequence. {@code membershipLevel} is the owner's capped membership level, from
     * which — with the owner's plain region — the {@code ownerSegment} is recomputed.
     */
    public static OwnerCreatedEvent next(Owner owner, int membershipLevel) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel, OwnerSegments.label(owner, membershipLevel),
                OWNER_CREATED);
    }

    /**
     * The owner's current primary identifier carried by the event: the {@code memberId}. The single
     * place that choice lives.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /** This event as its JSON object form, for the {@code AUDIT} logger. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
