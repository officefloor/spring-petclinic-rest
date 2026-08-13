package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(DailyOwnerRegistrations.bulkSignupWarning(ownerRepository));
        dto.setCapacityWarning(OwnerCityCounts.capacityWarning(ownerRepository, owner));
        dto.setRiskFlag(OwnerRiskFlag.of(ownerRepository, owner));
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
