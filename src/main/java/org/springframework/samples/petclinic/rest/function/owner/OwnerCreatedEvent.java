package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.util.OwnerIdentityVersion;

/**
 * Immutable structured audit event marking a successful owner creation, serialized to the
 * canonical schema-version-2 JSON object
 * {@code {"schemaVersion":2,"seq":..,"ownerId":..,"memberId":..,"membershipLevel":..,"event":"OWNER_CREATED"}}.
 *
 * <p>The {@code memberId} field carries the owner's <em>current primary identifier</em>
 * (see {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}), which
 * under version 2 is the version-2 member-id.
 *
 * <p>Emitted by {@link AuditOwnerCreated}; {@code seq} comes from {@link OwnerCreatedEventSequence}
 * and increases monotonically across creates.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel) {

    /** The event marker every serialized event carries. */
    public static final String EVENT = "OWNER_CREATED";

    /** This event as its canonical JSON object, keys in the documented order. */
    public String toJson() {
        return "{"
                + "\"schemaVersion\":" + OwnerIdentityVersion.AUDIT_SCHEMA_VERSION + ","
                + "\"seq\":" + this.seq + ","
                + "\"ownerId\":" + this.ownerId + ","
                + "\"memberId\":" + quote(this.memberId) + ","
                + "\"membershipLevel\":" + this.membershipLevel + ","
                + "\"event\":" + quote(EVENT)
                + "}";
    }

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
