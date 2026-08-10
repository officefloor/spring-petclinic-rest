package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.BusinessDay;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        // The effective registration date must fall on a business day, whether it was supplied or
        // defaulted. Everything derived from it (e.g. the membership number's year) reads it back.
        owner.setRegistrationDate(BusinessDay.adjust(registrationDate));
        built.set(owner);
    }
}
