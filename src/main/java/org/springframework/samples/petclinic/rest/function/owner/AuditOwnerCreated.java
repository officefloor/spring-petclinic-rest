package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits an audit trail line for a newly created owner. Runs after the owner has been
 * saved so the assigned id is available. The line is published to the dedicated
 * {@code AUDIT} logger and carries the owner id, the customerCode, the
 * registrationDate, the numeric membershipLevel and the membershipNumber.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = String.format("%s-M%02d", owner.getCustomerCode(),
                FiscalYear.yearSegment(owner.getRegistrationDate()));
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), membershipNumber);
    }
}
