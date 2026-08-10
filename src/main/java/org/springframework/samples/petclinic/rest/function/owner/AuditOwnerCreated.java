package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits an audit line on successful create via the dedicated {@code AUDIT} logger, carrying the
 * newly-assigned owner id, the {@code customerCode}, the {@code registrationDate} and the derived
 * {@code membershipLevel} and {@code membershipNumber}. Runs after {@code SaveOwner} so the owner id
 * is populated.
 *
 * <p>Alongside the human-readable line it emits an immutable structured {@link OwnerCreatedEvent} as
 * JSON on the same logger, carrying a {@code seq} that increases monotonically across creates and the
 * owner's current primary identifier (the {@code customerCode} today; whatever replaces it later).
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across all owner-created events for this application. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() + "-M"
            + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        int membershipLevel = MembershipLevel.level(MembershipLevel.points(owner.getEmail(),
            owner.getNamesakeCount(), owner.getHouseholdSize(), owner.getRegistrationDate(),
            LocalDate.now()));
        AUDIT.info("owner created id={} customerCode={} registrationDate={} "
                + "membershipLevel={} membershipNumber={}",
            owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
            membershipLevel, membershipNumber);

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQ.incrementAndGet(), owner.getId(),
            primaryIdentifier(owner), membershipLevel);
        AUDIT.info(event.toJson());
    }

    /** The owner's current primary identifier. Today this is the {@code customerCode}; when the
     *  customerCode is unified into the memberId, return the memberId here instead. */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }
}
