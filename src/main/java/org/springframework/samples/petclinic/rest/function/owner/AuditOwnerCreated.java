package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail line to the dedicated {@code AUDIT} logger once a new owner
 * has been persisted, recording the assigned owner id, the region-and-hash
 * {@code customerCode}, {@code registrationDate}, the derived {@code membershipLevel}
 * and the assigned {@code membershipNumber}.
 *
 * <p>Runs after {@link SaveOwner}, so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        logger.info("Owner created: id={} customerCode={} registrationDate={} "
                + "membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.membershipLevel(owner), owner.getMembershipNumber());
    }
}
