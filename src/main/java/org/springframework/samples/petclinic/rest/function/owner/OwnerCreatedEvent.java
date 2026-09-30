package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialized as the schema
 * version 2 JSON object {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel,
 * ownerSegment, event:'OWNER_CREATED'}}.
 *
 * <p>The {@code memberId} is the owner's <em>primary identifier</em> — the single value that uniquely
 * names the owner to the outside world, unifying the former customer code and membership number; under
 * the version-2 identity scheme it embeds the version-2 region code. The {@code ownerSegment} is
 * recomputed from the (version-2) owner via {@link Owner#getOwnerSegment()}, and {@link #schemaVersion}
 * pins the event schema at {@link #SCHEMA_VERSION 2}.
 *
 * <p>{@link #seq} is a process-wide, monotonically increasing sequence assigned at construction via
 * {@link #of(Owner)}, ordering events across every create regardless of pipeline or thread. The record
 * is immutable: once built, its fields never change.
 */
@JsonPropertyOrder({ "schemaVersion", "seq", "ownerId", "memberId", "membershipLevel", "ownerSegment",
        "event" })
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    /** The fixed event marker. */
    public static final String EVENT = "OWNER_CREATED";

    /** The current audit-event schema version. */
    public static final int SCHEMA_VERSION = 2;

    /** Monotonic sequence source, shared across all creates in the process. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /** Thread-safe serializer, shared across creates. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Builds the event for a persisted {@code owner}, assigning the next monotonic {@link #seq},
     * stamping the {@link #EVENT} marker and the {@link #SCHEMA_VERSION schema version}. The identifier
     * is taken from the owner's {@link Owner#getMemberId() memberId} — the owner's primary identifier —
     * and the {@code ownerSegment} is recomputed from the version-2 owner.
     */
    public static OwnerCreatedEvent of(Owner owner) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getMemberId(), owner.getMembershipLevel(), owner.getOwnerSegment(), EVENT);
    }

    /** Renders the event as a compact JSON object via the shared {@link ObjectMapper}. */
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
