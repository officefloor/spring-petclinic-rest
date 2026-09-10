package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;

/**
 * Emits an audit trail entry, via the dedicated {@code AUDIT} logger, for a
 * successfully created owner. The line carries the owner id, the assigned
 * customerCode, the registrationDate, the assigned membershipLevel and the
 * derived membershipNumber.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevels.of(owner), membershipNumber(owner));
    }

    private static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
    }
}
