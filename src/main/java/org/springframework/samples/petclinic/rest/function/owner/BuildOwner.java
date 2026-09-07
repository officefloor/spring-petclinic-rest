package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // The effective registration date (supplied or defaulted to today) must fall on a business
        // day: a weekend rolls forward to the next Monday. Everything derived from the registration
        // date (membership number, daily limit) then uses this adjusted date.
        owner.setRegistrationDate(BusinessDay.effectiveRegistrationDate(owner.getRegistrationDate()));
        built.set(owner);
    }
}
