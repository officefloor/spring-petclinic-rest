package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits an audit trail entry for a successful owner create in {@code POST /api/owners}.
 * Publishes a single line to the dedicated {@code AUDIT} logger carrying the persisted
 * owner's id, its assigned {@code customerCode}, its {@code registrationDate}, its assigned
 * {@code membershipLevel} and its derived {@code membershipNumber}. Runs after {@link SaveOwner}
 * so the generated id is available, and before the response is sent.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                owner.getMembershipLevel(), ownerMapper.membershipNumber(owner));
    }
}
