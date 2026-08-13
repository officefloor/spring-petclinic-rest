package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event for a successful owner create, emitted to the
 * {@code AUDIT} logger by {@link AuditOwnerCreated} alongside the human-readable audit
 * line. Serialized as the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>The {@code customerCode} slot carries the owner's <em>current primary
 * identifier</em> — sourced from {@link Owner#getPrimaryIdentifier()}, which is the
 * {@code customerCode} today and becomes the memberId once the two are unified. The
 * event therefore follows that change without edits here.
 *
 * <p>{@link #seq()} is a process-wide, monotonically increasing sequence stamped when
 * the event is built (see {@link #next(Owner)}); the record itself is immutable.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, int membershipLevel) {

    /** The {@code event} marker carried by every instance. */
    public static final String EVENT = "OWNER_CREATED";

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
            "{\"seq\":%d,\"ownerId\":%d,\"customerCode\":%s,\"membershipLevel\":%d,\"event\":\"%s\"}",
            this.seq, this.ownerId, jsonString(this.customerCode), this.membershipLevel, EVENT);
    }

    /** JSON string literal (with escaping) for {@code value}, or {@code null} when absent. */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
