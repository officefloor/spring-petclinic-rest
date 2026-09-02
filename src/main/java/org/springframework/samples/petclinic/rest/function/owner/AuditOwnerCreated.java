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
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            dto.getMembershipLevel(), dto.getMembershipNumber());
        AUDIT.info("{}", String.format(
            "{\"seq\":%d,\"ownerId\":%d,\"customerCode\":\"%s\",\"membershipLevel\":%d,\"event\":\"OWNER_CREATED\"}",
            SEQ.incrementAndGet(), owner.getId(), owner.getCustomerCode(), dto.getMembershipLevel()));
    }
}
