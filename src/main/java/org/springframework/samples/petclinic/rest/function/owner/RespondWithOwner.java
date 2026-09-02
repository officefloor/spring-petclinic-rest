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
        int namesakeCount = NamesakeCount.of(owner, ownerRepository);
        dto.setNamesakeCount(namesakeCount);
        int membershipPoints = MembershipPoints.of(owner, namesakeCount, HouseholdSize.of(owner, ownerRepository));
        dto.setMembershipPoints(membershipPoints);
        dto.setMembershipLevel(MembershipLevel.of(membershipPoints));
        dto.setLocality(Locality.of(owner));
        dto.setBulkSignupWarning(BulkSignupWarning.of(owner, ownerRepository));
        dto.setContactPreference(ContactPreference.of(owner));
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        response.send(dto);
    }
}
