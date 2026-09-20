package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.MembershipNumber;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits audit output for a successfully created owner. Runs after {@link SaveOwner} so the
 * generated id is available, writing both a human-readable audit line and an immutable
 * structured {@link OwnerCreatedEvent} (as JSON) to the dedicated {@code AUDIT} logger. The
 * structured event carries a monotonic {@code seq} from {@link AuditSequence}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, AuditSequence sequence) {
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} "
                        + "membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), MembershipNumber.of(owner));
        AUDIT.info(OwnerCreatedEvent.of(sequence.next(), owner).toJson());
    }
}
