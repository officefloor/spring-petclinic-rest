package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevels;
import org.springframework.samples.petclinic.util.MembershipNumbers;

/**
 * Emits an audit trail line for a newly created owner. Runs after {@link SaveOwner} has
 * persisted the entity, so the generated id is available, and records the owner id, the
 * assigned customer code, the effective registration date, the derived membership points,
 * level and number on the dedicated {@code AUDIT} logger. Purely a side-effect step; it leaves the
 * owner unchanged for the responder.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        int points = MembershipLevels.pointsOf(owner, LocalDate.now());
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipPoints={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                points, MembershipLevels.levelFor(points), MembershipNumbers.of(owner));
    }
}
