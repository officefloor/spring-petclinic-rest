package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the owner-created audit trail to the dedicated {@code AUDIT} logger once a new owner has
 * been persisted. Two things are emitted:
 * <ul>
 *   <li>a human-readable line recording the assigned owner id, the region-and-hash
 *       {@code customerCode}, {@code registrationDate}, the derived {@code membershipLevel} and the
 *       assigned {@code membershipNumber};</li>
 *   <li>an immutable structured {@link OwnerCreatedEvent} (as JSON), tagged with a monotonically
 *       increasing {@link #SEQUENCE sequence} across creates and carrying the owner's current
 *       primary identifier.</li>
 * </ul>
 *
 * <p>Runs after {@link SaveOwner}, so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence shared across all creates (and function instances). */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        int membershipLevel = ownerMapper.membershipLevel(owner);
        logger.info("Owner created: id={} customerCode={} registrationDate={} "
                + "membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, owner.getMembershipNumber());

        // The current primary identifier is the customerCode; a later unification into memberId
        // changes only this argument (and OwnerCreatedEvent's field), not this step's shape.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(),
                owner.getId(), owner.getCustomerCode(), membershipLevel);
        logger.info(event.toJson());
    }
}
