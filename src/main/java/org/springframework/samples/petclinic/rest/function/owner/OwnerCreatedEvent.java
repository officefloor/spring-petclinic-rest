package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable, structured audit event for a newly created owner, emitted (as JSON) to the
 * {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>The event carries the owner id and the owner's <em>current primary identifier</em>. Today
 * that identifier is the {@code customerCode}; when a later checkpoint unifies the customerCode
 * into a {@code memberId}, the caller passes the memberId here instead and the event carries it —
 * the shape of the event does not change, only the value fed into {@code customerCode}.
 *
 * <p>{@code seq} is a process-wide, monotonically increasing sequence across creates: each built
 * event takes the next value, so the order and count of creates is recoverable from the log.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The single event marker this type emits. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Process-wide monotonic sequence across all created owners. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /**
     * Build the next event for a saved owner.
     *
     * @param ownerId           the saved owner's id.
     * @param customerCode      the owner's current primary identifier (customerCode today).
     * @param membershipLevel   the owner's effective membership level.
     * @return an event stamped with the next sequence number.
     */
    public static OwnerCreatedEvent of(Integer ownerId, String customerCode, int membershipLevel) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), ownerId, customerCode, membershipLevel,
                OWNER_CREATED);
    }

    /** Render this event as a compact JSON object. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"customerCode\":" + quote(this.customerCode)
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
