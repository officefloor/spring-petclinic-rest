package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwner {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            int members = 0;
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    members++;
                }
            }
            if (members >= 3) {
                dto.setMembershipTier("GOLD");
            }
        }
        response.send(dto);
    }
}
