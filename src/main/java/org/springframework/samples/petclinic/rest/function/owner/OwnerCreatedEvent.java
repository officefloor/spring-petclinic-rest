package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted alongside the human-readable audit line when an owner is
 * created. Rendered as the JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:"OWNER_CREATED"}}.
 *
 * <p>{@code memberId} carries the owner's primary identifier, the unified
 * {@link Owner#getMemberId() memberId}.
 *
 * <p>Instances are immutable: fields are captured at construction and never mutated, so a recorded
 * event cannot be altered after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel) {

    /** Event marker carried in the {@code event} field. */
    public static final String EVENT = "OWNER_CREATED";

    /** Builds the event for a persisted owner, reading its unified {@code memberId} identifier. */
    public static OwnerCreatedEvent forOwner(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(),
                owner.getMembershipLevel());
    }

    /** Canonical JSON rendering of this event. */
    public String toJson() {
        StringBuilder json = new StringBuilder(96);
        json.append("{\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"memberId\":").append(quote(this.memberId))
                .append(",\"membershipLevel\":").append(this.membershipLevel)
                .append(",\"event\":").append(quote(EVENT))
                .append('}');
        return json.toString();
    }

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
                    }
                    else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
        return out.toString();
    }
}
