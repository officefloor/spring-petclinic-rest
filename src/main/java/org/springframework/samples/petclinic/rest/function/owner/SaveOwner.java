package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    private static final AtomicLong EVENT_SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        int namesakeCount = NamesakeCount.of(owner, ownerRepository);
        int membershipLevel = MembershipLevel.of(
            MembershipPoints.of(owner, namesakeCount, HouseholdSize.of(owner, ownerRepository)));
        String memberId = owner.getCustomerCode();
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), memberId, owner.getRegistrationDate(), membershipLevel);
        // Immutable structured event. Carries the owner's unified primary identifier as memberId.
        String event = String.format(
            "{\"seq\":%d,\"ownerId\":%d,\"memberId\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
            EVENT_SEQ.incrementAndGet(), owner.getId(), memberId, membershipLevel);
        AUDIT.info("{}", event);
        NOTIFY.info("welcome ownerId={} memberId={}", owner.getId(), memberId);
    }
}
