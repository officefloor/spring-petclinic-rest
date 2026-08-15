package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted alongside the human-readable audit line when a new owner
 * is created (see {@link AuditOwnerCreated}). Serialized to a single-line JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event}} where {@code event} is always
 * {@value #EVENT}.
 *
 * <p>{@code seq} is a process-wide, monotonically increasing sequence number handed out by
 * {@link #of(Owner, int)} — one per created owner, ordering the events regardless of which request
 * produced them.
 *
 * <p>The event carries the owner's <em>current primary identifier</em>, resolved once by
 * {@link #primaryIdentifierOf(Owner)}. Today that is the {@code customerCode}; when the customer
 * code is later unified into the {@code memberId}, only that accessor changes and every event then
 * carries the {@code memberId} instead — the callers and the serialized shape stay put.
 *
 * <p>Instances are immutable: all fields are captured at construction and never mutated.
 */
final class OwnerCreatedEvent {

    /** The fixed {@code event} discriminator carried by every owner-created event. */
    static final String EVENT = "OWNER_CREATED";

    /** Process-wide source of monotonically increasing sequence numbers across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final long seq;

    private final int ownerId;

    private final String primaryIdentifier;

    private final int membershipLevel;

    private OwnerCreatedEvent(long seq, int ownerId, String primaryIdentifier, int membershipLevel) {
        this.seq = seq;
        this.ownerId = ownerId;
        this.primaryIdentifier = primaryIdentifier;
        this.membershipLevel = membershipLevel;
    }

    /**
     * A new event for the just-saved {@code owner} at the given {@code membershipLevel}, drawing the
     * next sequence number. The owner must already be persisted so {@link Owner#getId()} is set.
     */
    static OwnerCreatedEvent of(Owner owner, int membershipLevel) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifierOf(owner), membershipLevel);
    }

    /**
     * The owner's current primary identifier. This is the single point that decides which field
     * value the event carries: the {@code customerCode} today, and whatever replaces it later.
     */
    private static String primaryIdentifierOf(Owner owner) {
        return owner.getCustomerCode();
    }

    /** This event as a compact, single-line JSON object. */
    String toJson() {
        return "{"
                + "\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"customerCode\":" + quote(this.primaryIdentifier)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":" + quote(EVENT)
                + "}";
    }

    /** JSON string literal for {@code value}, escaping quotes and backslashes, or {@code null}. */
    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.append('"').toString();
    }
}
