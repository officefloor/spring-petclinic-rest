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
        dto.setBulkSignupWarning(BulkSignup.warningToday(ownerRepository));
        String householdId = HouseholdId.of(owner.getLastName(), owner.getAddress());
        long members = ownerRepository.findAll().stream()
                .filter(o -> householdId.equals(HouseholdId.of(o.getLastName(), o.getAddress())))
                .count();
        if (members >= 3) {
            dto.setMembershipTier("GOLD");
        }
        response.send(dto);
    }
}
