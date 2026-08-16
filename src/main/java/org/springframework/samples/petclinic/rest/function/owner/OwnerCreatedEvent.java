package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted alongside the human-readable audit line when an owner is
 * created. Schema version 2, rendered as the JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, ownerSegment, schemaVersion:2, event:"OWNER_CREATED"}}.
 *
 * <p>{@code memberId} carries the owner's primary identifier, the unified
 * {@link Owner#getMemberId() memberId} (now derived under version 2). {@code ownerSegment} is the
 * owner's marketing segment recomputed from the version-2 identity (see {@link OwnerSegment}), and
 * {@code schemaVersion} pins the event to version 2 of this schema.
 *
 * <p>Instances are immutable: fields are captured at construction and never mutated, so a recorded
 * event cannot be altered after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, int schemaVersion) {

    /** Event marker carried in the {@code event} field. */
    public static final String EVENT = "OWNER_CREATED";

    /** The schema version stamped onto every event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds the version-2 event for a persisted owner, reading its unified {@code memberId}
     * identifier and recomputing its owner segment from the version-2 identity.
     */
    public static OwnerCreatedEvent forOwner(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(),
                owner.getMembershipLevel(), OwnerSegment.forOwner(owner), SCHEMA_VERSION);
    }

    /** Canonical JSON rendering of this event. */
    public String toJson() {
        StringBuilder json = new StringBuilder(128);
        json.append("{\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"memberId\":").append(quote(this.memberId))
                .append(",\"membershipLevel\":").append(this.membershipLevel)
                .append(",\"ownerSegment\":").append(quote(this.ownerSegment))
                .append(",\"schemaVersion\":").append(this.schemaVersion)
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
