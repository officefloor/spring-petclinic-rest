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
        if (isBlank(request.getAddressLine1()) && isBlank(request.getAddress())) {
            missing.add("address"); // an address in either form is required
        }
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        Owner owner = ownerMapper.toOwner(request);
        LocalDate effective = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDay.nextBusinessDay(effective));
        built.set(owner);
    }

    private static void require(String field, String value, List<String> missing) {
        if (isBlank(value)) {
            missing.add(field);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
