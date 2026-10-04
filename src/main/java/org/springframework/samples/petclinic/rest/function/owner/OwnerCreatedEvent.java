package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured event emitted to the {@code AUDIT} logger when a new owner is created,
 * alongside the human-readable audit line (see {@link AuditOwnerCreated}). Serialised as the JSON
 * object {@code {seq, ownerId, memberId, membershipLevel, event, schemaVersion}} with
 * {@code event == }{@value #EVENT} and {@code schemaVersion == }{@value #SCHEMA_VERSION}.
 *
 * <p>{@code seq} is a monotonically increasing sequence across creates (assigned by
 * {@link AuditOwnerCreated}), so consumers can order and de-duplicate events independently of log
 * timestamps.
 *
 * <p>The {@code memberId} field carries the owner's primary identifier: the unified
 * {@code memberId} ({@link OwnerMemberId}) — the record is the one place that definition lives.
 *
 * <p>Being a record, the event is deeply immutable once constructed.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel) {

    /** The {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit schema version carried by every owner-created event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * The canonical JSON form {@code {seq, ownerId, memberId, membershipLevel, event,
     * schemaVersion}}. Built directly rather than via a mapper so the audit side-effect carries no
     * dependency on the HTTP response serialisation.
     */
    public String toJson() {
        return "{\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"memberId\":" + quote(this.memberId)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":\"" + EVENT + "\""
                + ",\"schemaVersion\":" + SCHEMA_VERSION + "}";
    }

    /** A JSON string literal for {@code value}, escaping the characters JSON requires, or {@code null}. */
    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
