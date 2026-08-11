package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger for a successfully created owner,
 * carrying the owner id, the {@code customerCode}, the {@code registrationDate}, the
 * {@code membershipNumber} and the derived membership points and level. Runs after the owner has
 * been saved (so the generated id is available) and before the response is sent.
 */
public class LogOwnerAudit {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        String membershipNumber = owner.getCustomerCode() + "-M"
                + String.format("%02d", FiscalYear.yearSegment(owner.getRegistrationDate()));
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipNumber={} "
                        + "membershipPoints={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipNumber,
                MembershipLevel.points(owner), MembershipLevel.of(owner));
    }
}
