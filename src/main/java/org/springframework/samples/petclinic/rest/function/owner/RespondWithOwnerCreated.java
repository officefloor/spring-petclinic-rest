package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    private static final AtomicInteger SEQ = new AtomicInteger();

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        int namesakeCount = Namesakes.countBefore(owner, ownerRepository);
        int membershipPoints = MembershipLevel.points(owner, namesakeCount, Household.size(owner, ownerRepository));
        int membershipLevel = MembershipCap.apply(owner, MembershipLevel.level(membershipPoints), ownerRepository);
        dto.setNamesakeCount(namesakeCount);
        dto.setMembershipPoints(membershipPoints);
        dto.setMembershipLevel(membershipLevel);
        dto.setBulkSignupWarning(BulkSignup.isWarned(owner, ownerRepository));
        Integer possibleDuplicateOf = PossibleDuplicate.of(owner, ownerRepository);
        dto.setPossibleDuplicate(possibleDuplicateOf != null);
        dto.setPossibleDuplicateOf(possibleDuplicateOf);
        LoggerFactory.getLogger("AUDIT").info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel);
        LoggerFactory.getLogger("AUDIT").info(
                "{\"seq\":{},\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"event\":\"OWNER_CREATED\"}",
                SEQ.incrementAndGet(), owner.getId(), owner.getCustomerCode(), membershipLevel);
        LoggerFactory.getLogger("NOTIFY").info("welcome owner id={} memberId={}", owner.getId(), owner.getCustomerCode());
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
