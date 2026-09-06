package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.json.JsonMapper;

/**
 * Immutable structured audit event emitted once an owner has been created, alongside the
 * human-readable audit line. It is serialized to a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event}} on the dedicated {@code AUDIT}
 * logger (see {@link AuditOwnerCreated}).
 *
 * <p>{@code seq} is a monotonically increasing integer allocated across every create for the life
 * of the application (see {@link #SEQUENCE}). The event carries the owner's <em>current primary
 * identifier</em> — the {@code customerCode} today. That choice lives in one place,
 * {@link #primaryIdentifier(Owner)}: when the customer code is later unified into the memberId,
 * return the memberId there and the event carries the memberId instead.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The single kind of event this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Application-wide source of the monotonically increasing {@code seq}, shared across creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * The next event for {@code owner}, allocating the next {@code seq} from the shared
     * application-wide sequence. {@code membershipLevel} is the owner's capped membership level.
     */
    public static OwnerCreatedEvent next(Owner owner, int membershipLevel) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel, OWNER_CREATED);
    }

    /**
     * The owner's current primary identifier carried by the event: the {@code customerCode} today.
     * When the customer code is unified into the memberId, return the memberId here so the event
     * carries it instead — the single place that choice lives.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /** This event as its JSON object form, for the {@code AUDIT} logger. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
