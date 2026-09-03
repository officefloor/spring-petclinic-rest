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
        int namesakeCount = Namesakes.countBefore(owner, ownerRepository);
        dto.setNamesakeCount(namesakeCount);
        int membershipPoints = MembershipLevel.points(owner, namesakeCount, Household.size(owner, ownerRepository));
        dto.setMembershipPoints(membershipPoints);
        int membershipLevel = MembershipCap.apply(owner, MembershipLevel.level(membershipPoints), ownerRepository);
        dto.setMembershipLevel(membershipLevel);
        dto.setOwnerSegment(OwnerSegment.of(membershipLevel, owner));
        dto.setContactPreference(owner.getEmail() != null ? "EMAIL" : "PHONE");
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        dto.setSelfLink("/api/owners/" + owner.getId());
        long cityOwners = ownerRepository.findAll().stream()
                .filter(other -> owner.getCity() != null && owner.getCity().equalsIgnoreCase(other.getCity()))
                .count();
        dto.setCapacityWarning(cityOwners >= 40 && cityOwners < 50);
        response.send(dto);
    }
}
