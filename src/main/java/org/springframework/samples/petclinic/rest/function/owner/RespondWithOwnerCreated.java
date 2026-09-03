package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class RespondWithOwnerCreated {

    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("AUDIT");

    private static final org.slf4j.Logger NOTIFY = org.slf4j.LoggerFactory.getLogger("NOTIFY");

    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper, OwnerRepository ownerRepository,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        LocalDate today = BusinessDay.roll(LocalDate.now());
        long createdToday = ownerRepository.findAll().stream()
                .filter(other -> today.equals(other.getRegistrationDate())).count();
        dto.setBulkSignupWarning(createdToday > 80);
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), dto.getMembershipLevel());
        AUDIT.info("{\"seq\":{},\"schemaVersion\":2,\"ownerId\":{},\"memberId\":\"{}\",\"membershipLevel\":{},\"ownerSegment\":\"{}\",\"event\":\"OWNER_CREATED\"}",
                SEQ.incrementAndGet(), owner.getId(), owner.getMemberId(), dto.getMembershipLevel(), dto.getOwnerSegment());
        NOTIFY.info("welcome ownerId={} memberId={}", owner.getId(), owner.getMemberId());
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
