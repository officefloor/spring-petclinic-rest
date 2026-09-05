package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class SaveOwner {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final Logger NOTIFY = LoggerFactory.getLogger("NOTIFY");

    /** Monotonic sequence for OWNER_CREATED structured events, across all creates. */
    private static final AtomicInteger EVENT_SEQ = new AtomicInteger();

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        ownerRepository.save(owner);
        int membershipLevel = MembershipTier.level(MembershipTier.points(
                owner.getNamesakeCount(), owner.getEmail(), owner.getRegistrationDate()));
        String memberId = owner.getCustomerCode();
        // Owner segment recomputed for the version-2 audit schema from the plain region
        // (the 'V2' tag stays inside the identifiers, never in the segment's region).
        String ownerSegment = OwnerSegment.of(membershipLevel,
            Locality.of(owner.getCity(), owner.getPostcode()));
        AUDIT.info("Created owner id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), memberId, owner.getRegistrationDate(), membershipLevel);
        AUDIT.info("{\"seq\":{},\"schemaVersion\":2,\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"ownerSegment\":\"{}\",\"event\":\"OWNER_CREATED\"}",
            EVENT_SEQ.incrementAndGet(), owner.getId(), memberId, membershipLevel, ownerSegment);
        NOTIFY.info("Welcome owner id={} memberId={}", owner.getId(), memberId);
    }
}
