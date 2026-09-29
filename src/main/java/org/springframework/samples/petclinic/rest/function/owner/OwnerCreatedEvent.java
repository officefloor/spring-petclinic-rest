package org.springframework.samples.petclinic.rest.function.owner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable structured audit event emitted on a successful owner create, alongside the plain audit
 * line. {@link AuditOwnerCreated} serializes it with the project's Jackson {@code ObjectMapper} — the
 * same serializer the {@code rest/dto} DTOs use — to a single-line JSON object with fields in the
 * order the spec lists them: {@code {"seq":..,"ownerId":..,"memberId":..,"membershipLevel":..,"event":"OWNER_CREATED"}}.
 *
 * <p>The {@code memberId} slot carries the owner's <strong>primary identifier</strong>, read from
 * {@link org.springframework.samples.petclinic.model.Owner#primaryIdentifier()} — the unified member id.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates (see {@link AuditOwnerCreated}).
 */
@JsonPropertyOrder({ "seq", "ownerId", "memberId", "membershipLevel", "event" })
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, Integer membershipLevel) {

    /** The fixed event marker distinguishing this structured event from other AUDIT lines. */
    public static final String EVENT = "OWNER_CREATED";

    /** The constant event marker, exposed so it serializes as the {@code event} field. */
    @JsonProperty("event")
    public String event() {
        return EVENT;
    }
}
