package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

public class BuildOwner {

    public void service(@RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        require("address", request.getAddress(), missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        Owner owner = ownerMapper.toOwner(request);
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        built.set(owner);
    }

    private static void require(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
