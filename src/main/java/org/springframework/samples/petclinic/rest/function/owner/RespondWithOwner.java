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
        dto.setMembershipLevel(HouseholdMembershipCap.apply(owner, dto.getMembershipLevel(), ownerRepository, ownerMapper));
        dto.setSelfLink("/api/owners/" + owner.getId());
        dto.setBulkSignupWarning(BulkSignupWarning.isActive(owner, ownerRepository));
        dto.setCapacityWarning(CapacityWarning.isActive(owner, ownerRepository));
        dto.setContactPreference(ContactPreference.of(owner));
        dto.setIdentityKey(IdentityKey.of(owner));
        dto.setAgeBand(AgeBand.of(owner));
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        dto.setRiskFlag(RiskFlag.isActive(owner, ownerRepository));
        response.send(dto);
    }
}
