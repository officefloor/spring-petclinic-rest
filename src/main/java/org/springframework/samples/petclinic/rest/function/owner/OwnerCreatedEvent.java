package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event for a successful owner create, emitted to the
 * {@code AUDIT} logger by {@link AuditOwnerCreated} alongside the human-readable audit
 * line. Serialized as the JSON object
 * {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>The event is at schema version 2: it carries an explicit {@code schemaVersion} of 2,
 * and the {@code memberId} slot holds the owner's <em>primary identifier</em> — sourced
 * from {@link Owner#getPrimaryIdentifier()}, which is the version-2 memberId.
 *
 * <p>{@link #seq()} is a process-wide, monotonically increasing sequence stamped when
 * the event is built (see {@link #next(Owner)}); the record itself is immutable.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel) {

    /** The {@code event} marker carried by every instance. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version carried by every instance. */
    public static final int SCHEMA_VERSION = 2;

    private static final AtomicLong SEQUENCE = new AtomicLong();

    /**
     * Builds the next event for {@code owner}, stamping the next sequence number and
     * capturing the owner's current primary identifier and derived membership level.
     */
    public static OwnerCreatedEvent next(Owner owner) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
            owner.getPrimaryIdentifier(), owner.getMembershipLevel());
    }

    /** Compact JSON rendering with the fields in specification order. */
    public String toJson() {
        return String.format(
            "{\"schemaVersion\":%d,\"seq\":%d,\"ownerId\":%d,\"memberId\":%s,\"membershipLevel\":%d,\"event\":\"%s\"}",
            SCHEMA_VERSION, this.seq, this.ownerId, jsonString(this.memberId), this.membershipLevel, EVENT);
    }

    /** JSON string literal (with escaping) for {@code value}, or {@code null} when absent. */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
