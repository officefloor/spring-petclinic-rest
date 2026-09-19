package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit record for a successfully created owner to the dedicated {@code AUDIT}
 * logger, carrying the persisted owner id, its {@code customerCode}, its
 * {@code registrationDate} and its {@code membershipLevel}. Runs after {@link SaveOwner}
 * (so the generated id exists) and before {@link RespondWithOwnerCreated} responds.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        AUDIT.info("Owner created id={} customerCode={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel());
    }
}
