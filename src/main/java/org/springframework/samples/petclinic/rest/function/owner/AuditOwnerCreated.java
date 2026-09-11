package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYears;

/**
 * Emits an audit record for a freshly created owner via the dedicated {@code AUDIT}
 * logger. Runs after the owner is saved, so its generated id is populated. The line
 * carries the owner id, the assigned customerCode, the registrationDate, the
 * assigned membershipLevel and the membershipNumber.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() == null || owner.getRegistrationDate() == null
                ? null
                : owner.getCustomerCode() + "-M" + String.format("%02d", FiscalYears.yearSegment(owner.getRegistrationDate()));
        audit.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevels.forOwner(owner), membershipNumber);
    }
}
