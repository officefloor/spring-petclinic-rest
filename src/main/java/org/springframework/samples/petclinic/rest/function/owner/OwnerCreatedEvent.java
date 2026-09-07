package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event for a successful {@code POST /api/owners}. Rendered as the JSON
 * object <code>{seq, ownerId, memberId, membershipLevel, event}</code> and published on the
 * dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}, alongside the human-readable audit
 * line.
 *
 * <p>{@code seq} is a monotonically increasing integer assigned once per created owner, in creation
 * order, from a process-wide counter.
 *
 * <p>{@code memberId} carries the owner's <em>current primary identifier</em>. It is sourced from a
 * single accessor ({@link Owner#getMemberId()}).
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
        String event) {

    /** Discriminator recorded as the event's {@code event} field. */
    public static final String EVENT = "OWNER_CREATED";

    /** Process-wide source of the monotonically increasing {@code seq}. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    /**
     * Builds the next event for the given saved owner, allocating a fresh {@code seq}. The owner's
     * current primary identifier ({@link Owner#getMemberId()}) is captured as {@code memberId} and
     * its numeric {@link MembershipLevel} is captured as {@code membershipLevel}.
     */
    public static OwnerCreatedEvent of(Owner owner) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(), owner.getMemberId(),
                MembershipLevel.of(owner), EVENT);
    }

    /**
     * This event as a compact JSON object with fields in the order
     * {@code seq, ownerId, memberId, membershipLevel, event}.
     */
    public String toJson() {
        StringBuilder json = new StringBuilder(96);
        json.append('{');
        json.append("\"seq\":").append(seq);
        json.append(",\"ownerId\":").append(ownerId);
        json.append(",\"memberId\":").append(quote(memberId));
        json.append(",\"membershipLevel\":").append(membershipLevel);
        json.append(",\"event\":").append(quote(event));
        json.append('}');
        return json.toString();
    }

    /** JSON string literal for {@code value}, or the bare {@code null} literal when it is absent. */
    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder(value.length() + 2);
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
        return out.toString();
    }
}
