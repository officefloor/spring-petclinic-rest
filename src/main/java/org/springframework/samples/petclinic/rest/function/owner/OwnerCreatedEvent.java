package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable, structured audit event for a newly created owner, emitted (as JSON) to the
 * {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>The event carries the owner id and the owner's primary identifier, the unified
 * {@code memberId}.
 *
 * <p>{@code seq} is a process-wide, monotonically increasing sequence across creates: each built
 * event takes the next value, so the order and count of creates is recoverable from the log.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
        String event) {

    /** The single event marker this type emits. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Process-wide monotonic sequence across all created owners. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /**
     * Build the next event for a saved owner.
     *
     * @param ownerId           the saved owner's id.
     * @param memberId          the owner's primary identifier (the unified memberId).
     * @param membershipLevel   the owner's effective membership level.
     * @return an event stamped with the next sequence number.
     */
    public static OwnerCreatedEvent of(Integer ownerId, String memberId, int membershipLevel) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), ownerId, memberId, membershipLevel,
                OWNER_CREATED);
    }

    /** Render this event as a compact JSON object. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"memberId\":" + quote(this.memberId)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":" + quote(this.event)
                + "}";
    }

    private static String quote(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
