package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Final step of {@code POST /api/owners} that records the successful create on the dedicated
 * {@code AUDIT} logger. Runs after {@link SaveOwner} so the owner's generated {@code id} is
 * available; the line carries the owner id, its {@code customerCode}, its {@code registrationDate}
 * and its numeric {@code membershipLevel}.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        audit.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner.getNamesakeCount(), owner.getEmail(),
                        owner.getRegistrationDate()));
    }
}
