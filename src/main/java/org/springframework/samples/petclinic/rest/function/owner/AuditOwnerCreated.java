package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.FiscalYears;
import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on successful create via the dedicated {@code AUDIT} logger,
 * carrying the owner id, customerCode, registrationDate, membershipLevel and
 * membershipNumber. Runs after {@code save}, so the owner has a generated id.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevels.of(owner), membershipNumber(owner));
    }

    private static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", FiscalYears.startYear(owner.getRegistrationDate()) % 100);
    }
}
