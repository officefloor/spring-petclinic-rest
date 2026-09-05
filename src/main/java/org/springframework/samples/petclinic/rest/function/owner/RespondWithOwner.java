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
        dto.setMembershipLevel(MembershipCap.level(owner, dto.getMembershipLevel(), ownerRepository));
        dto.setBulkSignupWarning(BulkSignup.warningToday(ownerRepository));
        dto.setCapacityWarning(CityCapacity.approaching(owner.getCity(), ownerRepository));
        dto.setRiskFlag(RiskFlag.of(owner, ownerRepository));
        dto.setSelfLink("/api/owners/" + owner.getId());
        response.send(dto);
    }
}
