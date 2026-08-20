package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        LocalDate effective = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        // Registration must land on a business day: a supplied or defaulted weekend rolls forward
        // to Monday, and every value later derived from this date uses the adjusted value.
        owner.setRegistrationDate(BusinessDay.roll(effective));
        built.set(owner);
    }
}
