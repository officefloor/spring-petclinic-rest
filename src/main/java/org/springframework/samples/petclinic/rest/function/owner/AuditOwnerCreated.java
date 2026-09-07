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
 * <li>a human-readable audit line carrying the owner id, its {@code customerCode}, its
 * {@code registrationDate}, its numeric {@code membershipLevel} and its {@code membershipNumber}; and
 * <li>an immutable {@link OwnerCreatedEvent}, rendered as a JSON object
 * <code>{seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}</code>, where
 * {@code seq} increases monotonically across creates and {@code customerCode} carries the owner's
 * current primary identifier.
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String customerCode = owner.getCustomerCode();
        audit.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), customerCode, owner.getRegistrationDate(),
                MembershipLevel.of(owner),
                MembershipNumber.of(customerCode, owner.getRegistrationDate()));
        audit.info(OwnerCreatedEvent.of(owner).toJson());
    }
}
