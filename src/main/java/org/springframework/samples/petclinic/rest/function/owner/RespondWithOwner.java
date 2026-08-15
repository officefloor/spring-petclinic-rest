package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.util.MembershipLevel;

public class RespondWithOwner {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<OwnerDto> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        dto.setBulkSignupWarning(BulkSignup.warningFor(owner, ownerRepository));
        int points = MembershipLevel.pointsOf(owner, Household.memberCount(owner, ownerRepository));
        dto.setMembershipPoints(points);
        dto.setMembershipLevel(MembershipCap.cappedLevelFor(owner,
                MembershipLevel.levelOf(points), ownerRepository));
        response.send(dto);
    }
}
