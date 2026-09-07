package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Final step of {@code POST /api/owners} that records the successful create on the dedicated
 * {@code AUDIT} logger. Runs after {@link SaveOwner} so the owner's generated {@code id} is
 * available; the line carries the owner id, its {@code customerCode}, its {@code registrationDate},
 * its numeric {@code membershipLevel} and its {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner),
                MembershipNumber.of(owner.getCustomerCode(), owner.getRegistrationDate()));
    }
}
