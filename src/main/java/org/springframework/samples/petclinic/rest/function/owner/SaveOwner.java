package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonic sequence for OWNER_CREATED structured events, across all creates. */
    private static final AtomicInteger EVENT_SEQ = new AtomicInteger();

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        int membershipLevel = MembershipTier.level(MembershipTier.points(
                owner.getNamesakeCount(), owner.getEmail(), owner.getRegistrationDate()));
        // The owner's current primary identifier; becomes the memberId once unified.
        String primaryId = owner.getCustomerCode();
        AUDIT.info("Created owner id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel,
            MembershipNumber.of(owner.getCustomerCode(), owner.getRegistrationDate()));
        AUDIT.info("{\"seq\":{},\"ownerId\":{},\"customerCode\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
            EVENT_SEQ.incrementAndGet(), owner.getId(), primaryId, membershipLevel);
    }
}
