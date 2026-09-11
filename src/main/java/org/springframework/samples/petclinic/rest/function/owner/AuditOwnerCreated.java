package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on the dedicated {@code AUDIT} logger for a successfully
 * created owner, carrying the owner id, customerCode, registrationDate,
 * membershipLevel and membershipNumber. Runs after {@link SaveOwner} so the
 * generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) ? null
                : owner.getCustomerCode() + "-M" + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        audit.info(
                "Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                org.springframework.samples.petclinic.model.MembershipLevel.of(owner), membershipNumber);
    }
}
