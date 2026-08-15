package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted alongside the human-readable audit line when an owner is
 * created. Rendered as the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:"OWNER_CREATED"}}.
 *
 * <p>{@code customerCode} carries the owner's <em>current primary identifier</em>. Today that is the
 * {@link Owner#getCustomerCode() customerCode}; when the customerCode is later unified into a
 * {@code memberId}, only {@link #forOwner(long, Owner)} changes so the same field carries the
 * memberId instead — the event's shape and the way callers read the identifier stay the same.
 *
 * <p>Instances are immutable: fields are captured at construction and never mutated, so a recorded
 * event cannot be altered after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel) {

    /** Event marker carried in the {@code event} field. */
    public static final String EVENT = "OWNER_CREATED";

    /**
     * Builds the event for a persisted owner, reading its current primary identifier. This single
     * line is the switch point: replace {@code owner.getCustomerCode()} with the memberId once the
     * identifiers are unified and every emitted event follows.
     */
    public static OwnerCreatedEvent forOwner(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getCustomerCode(),
                owner.getMembershipLevel());
    }

    /** Canonical JSON rendering of this event. */
    public String toJson() {
        StringBuilder json = new StringBuilder(96);
        json.append("{\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"customerCode\":").append(quote(this.customerCode))
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
