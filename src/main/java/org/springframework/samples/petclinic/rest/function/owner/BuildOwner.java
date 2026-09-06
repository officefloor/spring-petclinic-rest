package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        // Normalize the telephone to E.164 form (keep '+'/country code, else assume '+61').
        request.setTelephone(Telephones.toE164(request.getTelephone()));
        // Email is optional; when present it must be syntactically valid and is stored lower-cased.
        request.setEmail(OwnerEmails.normalize(request.getEmail()));
        Owner owner = ownerMapper.toOwner(request);
        // Default the registration date to the server's current date when none is supplied.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        built.set(owner);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
