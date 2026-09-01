package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class RespondWithOwnerCreated {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request, OwnerMapper ownerMapper,
            OwnerRepository ownerRepository, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignupWarning.isActive(owner, ownerRepository));
        dto.setContactPreference(ContactPreference.of(owner));
        // A declared household member is not a suspected duplicate.
        Integer possibleDuplicateOf = Boolean.TRUE.equals(request.getSharesHousehold()) ? null
                : PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
