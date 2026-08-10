package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Builds the {@link Owner} entity from the request body previously bound and validated by
 * {@link ValidateOwnerFields}, which republishes it as a variable (so this step does not
 * bind {@code @RequestBody} a second time).
 *
 * <p>The owner's {@code registrationDate} is the effective, business-day-adjusted date resolved by
 * {@link ResolveRegistrationDate} (supplied in the request or defaulted to the server date, then
 * rolled off any weekend). This step stores that adjusted date so every created owner has a
 * {@code registrationDate} on a business day.
 */
public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val LocalDate registrationDate,
            OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        owner.setRegistrationDate(registrationDate);
        built.set(owner);
    }
}
