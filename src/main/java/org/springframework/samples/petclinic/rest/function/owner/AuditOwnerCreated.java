package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.MembershipNumber;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail entry once an owner has been persisted, capturing the
 * generated id together with the assigned customer code, registration date,
 * membership level and membership number.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), MembershipLevel.effective(owner),
                MembershipNumber.of(owner));
    }
}
