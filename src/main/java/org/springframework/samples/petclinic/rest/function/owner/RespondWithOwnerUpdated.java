package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerUpdated {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(OwnerBulkSignup.isWarning(ownerRepository, owner.getRegistrationDate()));
        response.send(ResponseEntity.status(HttpStatus.NO_CONTENT).body(dto));
    }
}
