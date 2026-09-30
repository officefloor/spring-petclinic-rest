package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialized as the JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>The {@code memberId} is the owner's <em>primary identifier</em> — the single value that uniquely
 * names the owner to the outside world, unifying the former customer code and membership number.
 *
 * <p>{@link #seq} is a process-wide, monotonically increasing sequence assigned at construction via
 * {@link #of(Owner)}, ordering events across every create regardless of pipeline or thread. The record
 * is immutable: once built, its fields never change.
 */
@JsonPropertyOrder({ "seq", "ownerId", "memberId", "membershipLevel", "event" })
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The fixed event marker. */
    public static final String EVENT = "OWNER_CREATED";

    /** Monotonic sequence source, shared across all creates in the process. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /** Thread-safe serializer, shared across creates. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Builds the event for a persisted {@code owner}, assigning the next monotonic {@link #seq} and
     * stamping the {@link #EVENT} marker. The identifier is taken from the owner's
     * {@link Owner#getMemberId() memberId} — the owner's primary identifier.
     */
    public static OwnerCreatedEvent of(Owner owner) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getMemberId(), owner.getMembershipLevel(), EVENT);
    }

    /** Renders the event as a compact JSON object via the shared {@link ObjectMapper}. */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
