package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // Store the effective registration date (supplied or defaulted, rolled to a business day).
        // Values derived from it (e.g. the membership number's year segment) are assigned by later
        // steps and so pick up this adjusted date.
        owner.setRegistrationDate(BusinessDays.effectiveRegistrationDate(owner.getRegistrationDate()));
        built.set(owner);
    }
}
