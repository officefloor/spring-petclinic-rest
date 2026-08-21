package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // The effective registration date — supplied or defaulted to the server date — must fall on
        // a business day, so a weekend date rolls forward to the next Monday. Everything derived from
        // the registration date (e.g. the membership number's year segment) then uses this value.
        owner.setRegistrationDate(BusinessDay.effective(owner.getRegistrationDate()));
        built.set(owner);
    }
}
