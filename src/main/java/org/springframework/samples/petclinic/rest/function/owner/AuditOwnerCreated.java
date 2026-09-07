package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Final step of {@code POST /api/owners} that records the successful create on the dedicated
 * {@code AUDIT} logger. Runs after {@link SaveOwner} so the owner's generated {@code id} is
 * available.
 *
 * <p>Two things are emitted, both on the {@code AUDIT} logger:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, its {@code memberId}, its
 * {@code registrationDate} and its numeric {@code membershipLevel}; and
 * <li>an immutable {@link OwnerCreatedEvent}, rendered as a JSON object
 * <code>{seq, schemaVersion:2, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}</code>,
 * where {@code seq} increases monotonically across creates, {@code schemaVersion} is the version-2
 * schema and {@code memberId} carries the owner's current primary (version-2) identifier.
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));
        audit.info(OwnerCreatedEvent.of(owner).toJson());
    }
}
