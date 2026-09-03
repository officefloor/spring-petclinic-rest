package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;

import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        int namesakeCount = Namesakes.countBefore(owner, ownerRepository);
        int membershipPoints = MembershipLevel.points(owner, namesakeCount, Household.size(owner, ownerRepository));
        int membershipLevel = MembershipLevel.level(membershipPoints);
        dto.setNamesakeCount(namesakeCount);
        dto.setMembershipPoints(membershipPoints);
        dto.setMembershipLevel(membershipLevel);
        dto.setBulkSignupWarning(BulkSignup.isWarned(owner, ownerRepository));
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        LoggerFactory.getLogger("AUDIT").info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel, MembershipNumber.of(owner));
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
