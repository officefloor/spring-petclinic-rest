package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final AtomicInteger SEQ = new AtomicInteger();

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        HouseholdTier.applyGold(owner, ownerRepository, dto);
        String ownerSegment = OwnerSegment.of(dto.getMembershipLevel(), Region.of(owner));
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), dto.getMembershipLevel());
        AUDIT.info("{}", String.format(
            "{\"seq\":%d,\"schemaVersion\":2,\"ownerId\":%d,\"memberId\":\"%s\",\"ownerSegment\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), owner.getCustomerCode(), ownerSegment, dto.getMembershipLevel()));
    }
}
