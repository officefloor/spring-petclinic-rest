package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits the audit side-effects to the dedicated {@code AUDIT} logger once a new owner has been
 * persisted. Runs after {@link SaveOwner} so the generated id is available, and emits two events:
 *
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the assigned {@code customerCode}, the
 * {@code registrationDate}, the derived {@code membershipLevel} and the assigned
 * {@code membershipNumber}; and
 * <li>an immutable, structured {@link OwnerCreatedEvent} as JSON, keyed by a monotonically
 * increasing sequence number and carrying the owner's current primary identifier.
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int points = MembershipLevel.pointsOf(owner, Household.memberCount(owner, ownerRepository));
        int level = MembershipCap.cappedLevelFor(owner, MembershipLevel.levelOf(points), ownerRepository);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                level, owner.getMembershipNumber());
        AUDIT.info("{}", OwnerCreatedEvent.of(owner, level).toJson());
    }
}
