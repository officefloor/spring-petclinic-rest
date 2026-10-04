package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /** Shared, thread-safe serializer for the structured event. */
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /** Monotonically increasing sequence across every owner create in this JVM. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        audit.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(),
                owner.getId(), primaryIdentifier(owner), owner.getMembershipLevel());
        audit.info("{}", MAPPER.writeValueAsString(event));
    }

    /**
     * Resolves the owner's current primary identifier — the single point that decides
     * what the event's identifier field carries. The primary identifier is the unified
     * {@code memberId}.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
