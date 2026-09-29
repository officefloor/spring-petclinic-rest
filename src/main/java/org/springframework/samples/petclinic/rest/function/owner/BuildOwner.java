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
        // Stamp the resolved business-day date, which already accounts for a supplied or
        // defaulted value rolled forward off a weekend (see ResolveRegistrationDate).
        owner.setRegistrationDate(registrationDate);
        built.set(owner);
    }
}
