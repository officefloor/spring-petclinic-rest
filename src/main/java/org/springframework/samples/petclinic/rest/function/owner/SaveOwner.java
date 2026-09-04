package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.mapper.MembershipNumber;
import org.springframework.samples.petclinic.mapper.MembershipPoints;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        String membershipNumber = MembershipNumber.of(owner.getCustomerCode(), owner.getRegistrationDate());
        int membershipLevel = MembershipLevel.of(MembershipPoints.of(owner.getNamesakeCount(),
                owner.getEmail(), owner.getRegistrationDate(), HouseholdTier.isGold(owner, ownerRepository)));
        audit.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, membershipNumber);
    }
}
