package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request whose firstName, lastName, address, city or telephone is
 * missing or blank, before {@link BuildOwner} runs. Publishes the body for later steps.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", request.getFirstName());
        addIfBlank(missing, "lastName", request.getLastName());
        addIfBlank(missing, "address", request.getAddress());
        addIfBlank(missing, "city", request.getCity());
        addIfBlank(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        // Normalise the telephone to E.164 form; reject when it cannot be formed.
        String telephone = E164Telephone.toE164(request.getTelephone());
        if (telephone == null) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        validated.set(request);
    }

    private static void addIfBlank(List<String> missing, String name, String value) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
