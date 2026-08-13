package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on successful owner creation, alongside the human-readable
 * audit line. Serialized to a single-line JSON object on the {@code AUDIT} logger:
 * {@code {seq, ownerId, customerCode, membershipLevel, event:"OWNER_CREATED"}}.
 *
 * <p>{@code customerCode} carries the owner's <em>current primary identifier</em>. Today that is the
 * {@link org.springframework.samples.petclinic.model.Owner#getCustomerCode() customerCode}; once the
 * customerCode is unified into the memberId the primary identifier — and thus this field's value —
 * becomes the memberId. See {@link AuditOwnerCreated} where the identifier is resolved in one place.
 *
 * <p>A record so the emitted event is immutable: once constructed its fields cannot change.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The only event kind emitted here. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, int ownerId, String customerCode, Integer membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, OWNER_CREATED);
    }

    /** Render as a compact, single-line JSON object. Field values here are simple identifiers and an
     *  integer level, so no characters need JSON string escaping. */
    public String toJson() {
        StringBuilder json = new StringBuilder(96);
        json.append("{\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"customerCode\":").append(quote(this.customerCode))
                .append(",\"membershipLevel\":").append(this.membershipLevel)
                .append(",\"event\":").append(quote(this.event))
                .append('}');
        return json.toString();
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
