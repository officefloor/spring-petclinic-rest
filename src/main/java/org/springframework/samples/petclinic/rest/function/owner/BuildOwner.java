package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.AddressRequiredException;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built,
            Out<Boolean> sharesHousehold)
            throws InvalidTelephoneException, AddressRequiredException, FutureRegistrationDateException {
        request.setTelephone(E164.normalize(request.getTelephone()));
        request.setAddress(Address.normalize(request.getAddress()));
        Owner owner = ownerMapper.toOwner(request);
        if (owner.getRegistrationDate() != null && owner.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException("registrationDate must not be later than the current date");
        }
        LocalDate registrationDate = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDay.rollForward(registrationDate));
        built.set(owner);
        sharesHousehold.set(Boolean.TRUE.equals(request.getSharesHousehold()));
    }
}
