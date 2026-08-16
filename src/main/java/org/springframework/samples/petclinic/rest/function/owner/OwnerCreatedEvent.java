package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The immutable {@code OWNER_CREATED} structured audit event, emitted alongside the human-readable
 * audit line (see {@link AuditOwnerCreated}) whenever an owner is created.
 *
 * <p>Serialized to JSON as
 * {@code {"schemaVersion":2,"seq":<n>,"ownerId":<id>,"customerCode":<identifier>,"membershipLevel":<level>,"ownerSegment":<segment>,"event":"OWNER_CREATED"}}.
 * This is schema version 2: it carries an explicit {@code schemaVersion} and the owner segment
 * recomputed from the version-2 identity. The {@code seq} is a process-wide monotonically increasing
 * integer across all creates. The {@code customerCode} carries the owner's <em>current primary
 * identifier</em> (see {@link OwnerIdentity#primaryIdentifier(Owner)}) — the version-2
 * {@code memberId}.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
        String ownerSegment, int schemaVersion) {

    /** The {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /** The structured audit schema version — 2 for the version-2 owner identity. */
    public static final int SCHEMA_VERSION = 2;

    /** Process-wide sequence source; each created owner's event gets the next value. */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /** Build the next event for a persisted {@code owner}, allocating a fresh {@link #seq()}. */
    public static OwnerCreatedEvent forOwner(Owner owner, OwnerRepository ownerRepository) {
        return new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                OwnerIdentity.primaryIdentifier(owner), MembershipLevel.of(owner, ownerRepository),
                OwnerSegment.of(owner, ownerRepository).getValue(), SCHEMA_VERSION);
    }

    /** The event as a single-line JSON object, in the field order stated on the type. */
    public String toJson() {
        return "{\"schemaVersion\":" + this.schemaVersion
                + ",\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"customerCode\":" + jsonString(this.customerCode)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"ownerSegment\":" + jsonString(this.ownerSegment)
                + ",\"event\":" + jsonString(EVENT)
                + "}";
    }

    /** A JSON string literal for {@code value} ({@code null} becomes JSON {@code null}). */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
