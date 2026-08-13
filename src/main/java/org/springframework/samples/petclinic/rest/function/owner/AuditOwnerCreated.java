package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits audit output to the dedicated {@code AUDIT} logger on a successful create:
 * first the human-readable audit line — carrying the new owner's id,
 * {@code customerCode}, {@code registrationDate}, numeric {@code membershipLevel} and
 * the derived {@code membershipNumber} — then an immutable structured
 * {@link OwnerCreatedEvent} rendered as JSON.
 *
 * <p>Runs after {@link SaveOwner} (so the id is assigned) and before
 * {@link RespondWithOwnerCreated} in the {@code POST /api/owners} pipeline.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = String.format("%s-M%s",
            owner.getCustomerCode(), owner.getFiscalYear().substring(2));
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            owner.getMembershipLevel(), membershipNumber);
        // Structured, machine-readable companion to the line above. Passed as a single
        // argument so the JSON braces are never treated as SLF4J placeholders.
        AUDIT.info("{}", OwnerCreatedEvent.next(owner).toJson());
    }
}
