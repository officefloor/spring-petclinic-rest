package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.web.bind.annotation.RequestBody;

public class BuildOwner {

    public void service(@RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException("Telephone must be exactly 10 digits");
        }
        request.setTelephone(digits);
        built.set(ownerMapper.toOwner(request));
    }
}
