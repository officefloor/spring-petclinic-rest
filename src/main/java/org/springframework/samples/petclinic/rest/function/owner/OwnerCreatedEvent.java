package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted, alongside the human-readable audit line, when an
 * owner is created (see {@link AuditOwnerCreated}). Serialised to a compact JSON object so the
 * audit trail carries a machine-readable record of every create.
 *
 * <p>The {@code customerCode} field carries the owner's <em>current primary identifier</em>. It
 * is the customerCode today; when that identity is unified into a memberId the producer supplies
 * the memberId instead, so the event always names whatever identifier is primary at the time.
 *
 * @param seq             monotonically increasing sequence number across all owner creates
 * @param ownerId         the created owner's id
 * @param customerCode    the owner's current primary identifier (the customerCode today)
 * @param membershipLevel the owner's membership level at creation
 * @param event           the event marker, always {@code OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The marker every owner-created event carries. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Render this event as a compact JSON object. */
    public String toJson() {
        return "{\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"customerCode\":" + quote(this.customerCode)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":" + quote(this.event)
                + "}";
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
