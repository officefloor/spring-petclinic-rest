package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, on successful create via the dedicated {@code AUDIT} logger, both an audit line — carrying
 * the new owner's id, its {@code customerCode}, its {@code registrationDate}, its
 * {@code membershipLevel} and its {@code membershipNumber} — and an immutable structured
 * {@link OwnerCreatedEvent} rendered as JSON.
 *
 * <p>The event's {@code seq} is a process-wide monotonically increasing integer across creates, and
 * its identifier is the owner's {@link Owner#getPrimaryIdentifier() primary identifier} (the
 * customerCode today), so the event follows that identifier as it evolves.
 *
 * <p>Runs after {@link SaveOwner}, so the owner has been assigned its generated id, and before
 * {@link RespondWithOwnerCreated}, so both are written only once the create has succeeded.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing event sequence, shared across all creates in this process. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info(
                "owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), membershipNumber(owner));

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getPrimaryIdentifier(), MembershipLevel.of(owner));
        AUDIT.info(event.toJson());
    }

    private static String membershipNumber(Owner owner) {
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
    }
}
