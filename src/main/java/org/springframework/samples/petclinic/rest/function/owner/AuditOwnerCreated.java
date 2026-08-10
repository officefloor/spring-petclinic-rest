package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line on successful create via the dedicated {@code AUDIT} logger, carrying the
 * new owner's id, its {@code customerCode}, its {@code registrationDate}, its
 * {@code membershipLevel} and its {@code membershipNumber}.
 *
 * <p>Runs after {@link SaveOwner}, so the owner has been assigned its generated id, and before
 * {@link RespondWithOwnerCreated}, so the line is only written once the create has succeeded.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info(
                "owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevel.of(owner), membershipNumber(owner));
    }

    private static String membershipNumber(Owner owner) {
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }
}
