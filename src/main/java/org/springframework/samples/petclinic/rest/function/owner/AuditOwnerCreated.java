package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.mapper.OwnerSegment;
import org.springframework.samples.petclinic.model.Owner;

public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence number across all created owners. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));

        // Schema-version-2 structured event: carries the owner's current primary identifier (the
        // version-2 memberId) and the owner segment recomputed from that version-2 identity.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), MembershipLevel.effective(owner),
                OwnerSegment.of(owner).name());
        AUDIT.info(event.toJson());
    }

    /** The owner's current primary identifier — the memberId. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }
}
