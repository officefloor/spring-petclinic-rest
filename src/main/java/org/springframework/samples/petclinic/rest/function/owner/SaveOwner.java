package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        audit.info("owner created id={} customerCode={} registrationDate={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
