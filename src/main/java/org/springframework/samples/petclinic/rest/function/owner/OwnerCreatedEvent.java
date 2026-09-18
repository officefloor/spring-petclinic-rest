package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured record of an owner's creation, emitted to the {@code AUDIT} logger as
 * JSON alongside the human-readable audit line (see {@link AuditOwnerCreated}).
 *
 * <p>The {@code memberId} field carries the owner's current primary identifier
 * (see {@link OwnerIdentity#primary(Owner)}), resolved in one place so the event always
 * follows that identifier. {@code ownerSegment} is recomputed from that version-2 identity —
 * its region read back from the member id (see {@link MemberId#region(String)}), which is the
 * plain region code — so the audit trail records the same segment the owner response carries.
 *
 * <p>{@code schemaVersion} is {@link IdentityVersion#AUDIT_SCHEMA_VERSION}, marking the
 * version-2 shape of this record.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    /** The event marker for an owner creation. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Build the event for {@code owner}, stamped with the given monotonic {@code seq}. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        String ownerSegment = OwnerSegment.of(owner.getMembershipLevel(),
                MemberId.region(owner.getMemberId()));
        return new OwnerCreatedEvent(IdentityVersion.AUDIT_SCHEMA_VERSION, seq, owner.getId(),
                OwnerIdentity.primary(owner), owner.getMembershipLevel(), ownerSegment, OWNER_CREATED);
    }
}
