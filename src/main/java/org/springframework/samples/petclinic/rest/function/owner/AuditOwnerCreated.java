package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger on successful create, recording the
 * newly-persisted owner's id, {@code customerCode}, {@code registrationDate}, {@code membershipLevel}
 * and {@code membershipNumber}. Runs after {@link SaveOwner} (so the owner has its generated id) and
 * before {@link RespondWithOwnerCreated}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                ownerMapper.toOwnerDto(owner).getMembershipLevel(), owner.getMembershipNumber());
    }
}
