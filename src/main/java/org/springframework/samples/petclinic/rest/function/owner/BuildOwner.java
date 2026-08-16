package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val LocalDate registrationDate,
            OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // Use the effective registration date resolved and rolled onto a business day upstream
        // (see ResolveRegistrationDate), overriding whatever was supplied in the request body.
        owner.setRegistrationDate(registrationDate);
        built.set(owner);
    }
}
