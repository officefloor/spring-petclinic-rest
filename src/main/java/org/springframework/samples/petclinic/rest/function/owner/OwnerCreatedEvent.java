package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialized as the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>The {@code customerCode} is the owner's <em>current primary identifier</em> — the value that
 * uniquely names the owner to the outside world. When a later evolution unifies the customer code into
 * a {@code memberId}, this event should move with it: rename the component (and its factory source in
 * {@link #of(Owner)}) so both the field and the serialized key track the owner's primary identity. The
 * concept is deliberately named {@code customerCode} in one place only, so that migration is a single
 * consistent rename rather than a hunt across an abstract field name, a hardcoded getter and a string
 * literal.
 *
 * <p>{@link #seq} is a process-wide, monotonically increasing sequence assigned at construction via
 * {@link #of(Owner)}, ordering events across every create regardless of pipeline or thread. The record
 * is immutable: once built, its fields never change.
 */
@JsonPropertyOrder({ "seq", "ownerId", "customerCode", "membershipLevel", "event" })
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
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
     * {@link Owner#getCustomerCode() customerCode} — the owner's current primary identifier.
     */
    public static OwnerCreatedEvent of(Owner owner) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getCustomerCode(), owner.getMembershipLevel(), EVENT);
    }

    /** Renders the event as a compact JSON object via the shared {@link ObjectMapper}. */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
