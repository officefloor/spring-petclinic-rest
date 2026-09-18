package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of the create/update owner pipelines. Normalizes the telephone to canonical
 * E.164 form and writes it back onto the body, so the persisted and returned
 * {@code telephone} is that E.164 string. A telephone that cannot form a valid E.164 number
 * throws {@link InvalidOwnerFieldsException} for a 400. Because it must run before the DTO's
 * pattern check would reject the raw input, this is the pipeline's sole {@code @RequestBody}
 * binding; the normalized body is published for {@link ValidateOwnerFields}.
 */
public class NormalizeOwnerTelephone {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized)
            throws InvalidOwnerFieldsException {

        String e164 = OwnerTelephone.toE164(request.getTelephone())
                .orElseThrow(() -> new InvalidOwnerFieldsException(List.of("telephone")));

        request.setTelephone(e164);
        normalized.set(request);
    }
}
