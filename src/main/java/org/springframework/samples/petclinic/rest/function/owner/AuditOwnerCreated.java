package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger once the owner has been
 * persisted, recording the assigned id, the {@code customerCode}, the
 * {@code registrationDate}, the {@code membershipPoints} and the {@code membershipLevel}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int points = Memberships.membershipPoints(owner, Households.size(owner, ownerRepository));
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipPoints={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                points, Memberships.membershipLevel(points));
    }
}
