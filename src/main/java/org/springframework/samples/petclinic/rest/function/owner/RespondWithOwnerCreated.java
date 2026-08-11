package org.springframework.samples.petclinic.rest.function.owner;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.util.MembershipLevels;
import org.springframework.samples.petclinic.util.OwnerCreatedEvent;
import org.springframework.samples.petclinic.util.OwnerIdentities;

public class RespondWithOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all owner-created events in this JVM. */
    private static final AtomicLong EVENT_SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerRepository ownerRepository, OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        HouseholdMembers.stamp(owner, ownerRepository);
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        int membershipLevel = MembershipLevels.cappedLevelFor(owner);
        AUDIT.info("Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, owner.getMembershipNumber());
        // In addition to the audit line, emit the immutable structured event. Its customerCode field
        // carries the owner's current primary identifier, so it follows a later switch to memberId.
        OwnerCreatedEvent event = new OwnerCreatedEvent(EVENT_SEQ.incrementAndGet(), owner.getId(),
                OwnerIdentities.primaryIdentifier(owner), membershipLevel);
        AUDIT.info(event.toJson());
        response.send(ResponseEntity.created(URI.create("/api/owners/" + owner.getId())).body(dto));
    }
}
