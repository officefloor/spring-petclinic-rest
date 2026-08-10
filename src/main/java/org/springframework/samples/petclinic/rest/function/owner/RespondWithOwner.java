package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<OwnerDto> response) {
        HouseholdMembers.stamp(owner, ownerRepository);
        response.send(ownerMapper.toOwnerDto(owner));
    }
}
