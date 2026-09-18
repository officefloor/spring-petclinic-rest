package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Normalizes the telephone by stripping every
 * non-digit character, then requires exactly ten digits; the normalized value is written
 * back onto the body so the persisted and returned {@code telephone} is those ten digits.
 * A telephone that is not exactly ten digits after stripping throws
 * {@link InvalidOwnerFieldsException} for a 400. Because it must run before the DTO's
 * digits-only pattern check would reject the raw input, this is the pipeline's sole
 * {@code @RequestBody} binding; the normalized body is published for {@link ValidateOwnerFields}.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized)
            throws InvalidOwnerFieldsException {

        String digits = OwnerTelephone.digits(request.getTelephone());
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }

        request.setTelephone(digits);
        normalized.set(request);
    }
}
