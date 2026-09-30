package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits, for each newly created owner, two records on the dedicated {@code AUDIT} logger:
 * a human-readable audit line (id, member id, registration date and membership level), and an
 * immutable structured {@link OwnerCreatedEvent} serialised as JSON so downstream consumers can
 * parse it reliably. Runs after the owner has been saved so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence number across creates. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, ObjectMapper mapper) {
        AUDIT.info("Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = OwnerCreatedEvent.of(SEQ.incrementAndGet(), owner);
        AUDIT.info(mapper.writeValueAsString(event));
    }
}
