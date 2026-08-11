package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        HouseholdMembers.stamp(owner, ownerRepository);
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                org.springframework.samples.petclinic.util.MembershipLevels.cappedLevelFor(owner),
                owner.getMembershipNumber());
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
