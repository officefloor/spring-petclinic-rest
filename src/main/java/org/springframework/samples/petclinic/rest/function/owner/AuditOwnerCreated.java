package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger recording a successful owner create.
 * The line carries the newly assigned owner id, the {@code customerCode}, the effective
 * {@code registrationDate}, the {@code membershipLevel} and the {@code membershipNumber}. Runs
 * after {@link SaveOwner} so the owner has an id.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner, ownerRepository), owner.getMembershipNumber());
    }
}
