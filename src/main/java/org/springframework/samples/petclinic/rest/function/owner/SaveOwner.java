package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        int namesakeCount = NamesakeCount.of(owner, ownerRepository);
        int membershipLevel = MembershipLevel.of(
            MembershipPoints.of(owner, namesakeCount, HouseholdSize.of(owner, ownerRepository)));
        String membershipNumber = owner.getCustomerCode() + "-M" + FiscalYear.yy(owner);
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel, membershipNumber);
    }
}
