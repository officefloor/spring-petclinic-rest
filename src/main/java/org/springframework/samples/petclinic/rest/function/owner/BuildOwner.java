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
        LocalDate effective = owner.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        // Roll the effective date (supplied or defaulted) onto a business day, so every
        // value derived from it — e.g. the membership number's year segment — matches.
        owner.setRegistrationDate(BusinessDay.adjust(effective));
        built.set(owner);
    }
}
