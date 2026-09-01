package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.RegistrationDateInFutureException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper,
            Out<Owner> built, Out<OwnerFieldsDto> requestOut) throws RegistrationDateInFutureException {
        Owner owner = ownerMapper.toOwner(request);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        else if (owner.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new RegistrationDateInFutureException("Registration date must not be later than today");
        }
        built.set(owner);
        requestOut.set(request);
    }
}
