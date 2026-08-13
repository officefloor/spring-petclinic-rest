package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, on successful create, both a human-readable audit line and an immutable structured
 * {@link OwnerCreatedEvent} — recording the newly-persisted owner's id, {@code memberId},
 * {@code registrationDate} and {@code membershipLevel} — to the dedicated {@code AUDIT} logger. Runs
 * after {@link SaveOwner} (so the owner has its generated id) and before
 * {@link RespondWithOwnerCreated}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all creates, stamped onto each emitted event. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        Integer membershipLevel = ownerMapper.toOwnerDto(owner).getMembershipLevel();
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), membershipLevel);
        AUDIT.info(event.toJson());
    }

    /**
     * The owner's primary identifier carried by the event — the unified {@code memberId}. This is the
     * one place that resolution is expressed.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
