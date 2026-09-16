package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.audit.AuditEventSequence;
import org.springframework.samples.petclinic.rest.audit.OwnerCreatedEvent;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits the audit trail for a successful owner create. Runs after {@link SaveOwner} has
 * assigned the owner's id, logging to the dedicated {@code AUDIT} logger both a human-readable
 * audit line and an immutable {@link OwnerCreatedEvent} serialized as JSON.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper, AuditEventSequence sequence,
            ObjectMapper objectMapper) {
        int membershipLevel = ownerMapper.membershipLevel(owner);
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);
        OwnerCreatedEvent event = OwnerCreatedEvent.of(sequence.next(), owner, membershipLevel);
        AUDIT.info(objectMapper.writeValueAsString(event));
    }
}
