package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.mapper.MembershipPoints;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private static final Logger notify = LoggerFactory.getLogger("NOTIFY");

    private static final AtomicLong EVENT_SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        int membershipLevel = MembershipLevel.of(MembershipPoints.of(owner.getNamesakeCount(),
                owner.getEmail(), owner.getRegistrationDate(), HouseholdTier.isGold(owner, ownerRepository)));
        // Unified primary identifier of the owner.
        String memberId = owner.getCustomerCode();
        audit.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), memberId, owner.getRegistrationDate(), membershipLevel);
        audit.info(String.format(
                "{\"seq\":%d,\"ownerId\":%d,\"memberId\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
                EVENT_SEQ.incrementAndGet(), owner.getId(), memberId, membershipLevel));
        notify.info("welcome owner id={} memberId={}", owner.getId(), memberId);
    }
}
