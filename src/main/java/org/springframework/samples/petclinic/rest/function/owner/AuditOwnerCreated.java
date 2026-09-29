package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Emits an audit line via the dedicated {@code AUDIT} logger on successful create, carrying
 * the persisted owner's id, {@code customerCode} and {@code registrationDate}, plus the derived
 * {@code membershipLevel} and {@code membershipNumber}. Wired after {@link SaveOwner} so the id is
 * assigned, and after the steps that set the customer code and business-day registration date, so
 * every value logged matches what {@code GET /owners/{id}} later returns.
 *
 * <p>The membership fields are derived, not stored. Rather than re-derive them here, this step reads
 * the {@link OwnerDto} published by {@link MapOwner}, the same DTO {@link RespondWithOwnerCreated}
 * sends, so the logged values and the response body cannot drift apart.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, @Val OwnerDto dto) {
        AUDIT.info("owner created id={} customerCode={} registrationDate={} membershipLevel={} "
                + "membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                dto.getMembershipLevel(), dto.getMembershipNumber());
    }
}
