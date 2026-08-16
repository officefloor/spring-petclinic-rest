package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Maps the validated body (published by {@link RequireOwnerFields}) to a new {@link Owner}.
 * When the body supplies no registration date, defaults it to the server's current date; either
 * way the effective date is rolled forward off any weekend to the next business day, so every
 * later step (membership number, daily limit) sees the adjusted {@code registrationDate}.
 */
public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        owner.setRegistrationDate(RegistrationDate.effective(request));
        built.set(owner);
    }
}
