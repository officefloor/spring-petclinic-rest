package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence number across all created owners. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() == null || owner.getRegistrationDate() == null
                ? null
                : owner.getCustomerCode() + "-M" + FiscalYear.yearSegment(owner.getRegistrationDate());
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), membershipNumber);

        // Immutable structured event carrying the owner's current primary identifier. Today that is
        // the customerCode; when it is unified into the memberId, source the identifier there instead.
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), MembershipLevel.effective(owner));
        AUDIT.info(event.toJson());
    }

    /** The owner's current primary identifier — the customerCode while that is the identity. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
