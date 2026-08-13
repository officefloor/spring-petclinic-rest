package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Builds the {@link Owner} entity from the request body published by
 * {@link ValidateOwnerFields} (which is the single {@code @RequestBody} reader in the
 * {@code POST /api/owners} pipeline).
 *
 * <p>The registration date is always the effective, business-day-adjusted date resolved
 * by {@link ResolveRegistrationDate} (supplied-or-defaulted, rolled off weekends), so
 * every created owner is persisted (and returned) with a business-day registration date
 * and every value derived from it — such as the membership number's year segment — uses
 * the adjusted date.
 */
public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val LocalDate registrationDate,
            OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        owner.setRegistrationDate(registrationDate);
        built.set(owner);
    }
}
