package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger recording a successful owner
 * create. Runs after {@link SaveOwner} (so the owner id is assigned) and before the
 * responder. The line carries the owner id, the {@code memberId}, the
 * {@code registrationDate} and the {@code membershipLevel}.
 *
 * <p>Besides that human-readable line it emits an immutable structured {@link OwnerCreatedEvent}
 * as JSON to the same logger, with a {@code seq} that increases monotonically across creates.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Monotonically increasing sequence across all owner creates. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        // The owner's primary identifier is the unified memberId.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                owner.getMemberId(), owner.getMembershipLevel(), OwnerCreatedEvent.OWNER_CREATED);
        AUDIT.info("{}", MAPPER.writeValueAsString(event));
    }
}
