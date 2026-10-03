package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold) {
        Owner owner = ownerMapper.toOwner(request);
        // Establish the raw registration date, defaulting an absent one to the server
        // date. NormalizeOwnerRegistrationDate later rolls a weekend forward to a
        // business day; derived values (membership number, daily limit) read it back
        // off the owner, so they see the adjusted date regardless.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        built.set(owner);
        // sharesHousehold is a create-time flag, not an Owner field; republish it for
        // RejectDuplicateHousehold. Absent in the body means false.
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
    }
}
