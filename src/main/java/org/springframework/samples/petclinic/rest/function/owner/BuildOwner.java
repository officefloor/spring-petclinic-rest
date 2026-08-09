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
        // The effective registration date has already been resolved (supplied or defaulted) and
        // rolled onto a business day by NormalizeRegistrationDate; store that adjusted value.
        owner.setRegistrationDate(registrationDate);
        built.set(owner);
    }
}
