package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger once an owner has been
 * persisted. Runs after {@link SaveOwner} so the owner id is assigned, and carries the
 * id, the {@code customerCode}, the {@code registrationDate}, the {@code membershipLevel}
 * and the {@code membershipNumber}. The membership fields are read from the mapped
 * {@link OwnerDto} so they match the values the endpoint reports.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerMapper ownerMapper) {
        OwnerDto dto = ownerMapper.toOwnerDto(owner);
        AUDIT.info(
                "owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                dto.getMembershipLevel(), dto.getMembershipNumber());
    }
}
